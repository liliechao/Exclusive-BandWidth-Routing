package org.opendaylight.controller.myrouting;

import org.opendaylight.controller.clustering.services.IClusterContainerServices;
import org.opendaylight.controller.forwardingrulesmanager.FlowEntry;
import org.opendaylight.controller.sal.core.Bandwidth;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Path;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.routing.IListenRoutingUpdates;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.topology.TopoEdgeUpdate;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.opendaylight.controller.topologymanager.ITopologyManagerClusterWideAware;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.Map;
//import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections15.Transformer;

@SuppressWarnings("rawtypes")
public class MyRoutingImpl
        implements IRouting, ITopologyManagerClusterWideAware {

    // 两种图，一种topo图，一种路径图
    // routingAware、switchManager、topologyManager、clusterContainerService属性的赋值由Activator.java向sal配置实例完成。
    private static Logger log = LoggerFactory.getLogger(MyRoutingImpl.class);
    private ConcurrentMap<Short, Graph<Node, Edge>> topologyBWAware; // 包含拓扑图
    private ConcurrentMap<Short, DijkstraShortestPath<Node, Edge>> sptBWAware; // 包含路径图
    DijkstraShortestPath<Node, Edge> mtp; // Max Throughput Path
    private Set<IListenRoutingUpdates> routingAware;
    private ISwitchManager switchManager;
    private ITopologyManager topologyManager;
    private static final long DEFAULT_LINK_SPEED = Bandwidth.BW1Gbps;
    private IClusterContainerServices clusterContainerService;
    public Vector<Path> randomusedpath = new Vector<Path>();
    public Vector<Path> weightusedpath = new Vector<Path>();
    //public HashMap<Path,Short> pathflowbwmap = new HashMap<Path,Short>();
    Map<Edge, Set<Property>> localedgesmap;
    //Random random = new Random();
    public ConcurrentHashMap<FlowEntry,Path> firstFEPathMap;
    public ConcurrentHashMap<FlowEntry,Short> firstFEBwMap;
    public ConcurrentHashMap<FlowEntry,LinkedList<FlowEntry>> firstFEsMap;
    public ArrayList<ArrayList> dropVector;
    public Vector<FlowEntry> disOverLapFlowEntry = new Vector<FlowEntry>();
    //public HashSet<HashMap<Node,Double>> nodetime = new HashSet<HashMap<Node,Double>>();

    // topo（graph）与spt（path）贯穿这个文件始终，它们分别与bw（带宽）形成map。

    public boolean creatBwTopo(short bw) {
        if (localedgesmap == null)
            localedgesmap = topologyManager.getEdges();
        Graph<Node, Edge> topo = this.topologyBWAware.get(new Short((short) 0));//可能不包含所有的边呢
        if (bw != 0) {
            this.topologyBWAware.remove(bw);
        }
        Collection<Edge> topoedges = topo.getEdges();
        for (Iterator<Edge> i = topoedges.iterator(); i.hasNext();) {
            Edge topoedge = i.next();
            Set<Property> localprops = localedgesmap.get(topoedge);
            bwEdgeUpdate(topoedge, UpdateType.ADDED, localprops, bw);
        }
        return true;
    }

    private boolean bwEdgeUpdate(Edge e, UpdateType type,
            Set<Property> localprops, short bw) {
        //log.info("Routing bwEdgeUpdate_single() is called");
        String srcType = null;
        String dstType = null;

        log.trace("Got an edgeUpdate: {} props: {} update type: {}",
                new Object[] { e, localprops, type });

        if ((e == null) || (type == null)) {
            log.error("Edge or Update type are null!");
            return false;
        } else {
            srcType = e.getTailNodeConnector().getType();
            dstType = e.getHeadNodeConnector().getType();

            if (srcType.equals(NodeConnector.NodeConnectorIDType.PRODUCTION)) {
                log.debug("Skip updates for {}", e);
                return false;
            }

            if (dstType.equals(NodeConnector.NodeConnectorIDType.PRODUCTION)) {
                log.debug("Skip updates for {}", e);
                return false;
            }
        }

        boolean newEdge = false;
        if (localprops != null) {
            if (localprops.remove(new Bandwidth((long) (10*Math.pow(10, 9))))){
                localprops.add(new Bandwidth((long) (1*Math.pow(10, 9))));
            }
            for (Iterator it = localprops.iterator(); it.hasNext();) {
                Object remainbw = it.next();
                if (remainbw instanceof Bandwidth) {
                    if (((Bandwidth) remainbw).getValue() < bw
                            * Math.pow(10, 6)) {
                        return false;
                    }
                }
            }
        }

        newEdge = !updateTopo(e, bw, type);

        /*
         * Bandwidth bw = new Bandwidth(0); boolean newEdge = false; if (props
         * != null) { props.remove(bw); } Short baseBW = Short.valueOf((short)
         * 0); // Update base topo newEdge = !updateTopo(e, baseBW, type);
         * log.info("baseBW"); if (newEdge == true) { if (bw.getValue() !=
         * baseBW) { // Update BW topo updateTopo(e, (short) bw.getValue(),
         * type); log.info("bw"); } }
         */
        return newEdge;
    }

    public void bwUpdate(Path paths, short bw,boolean minusOrPlus) {
        List<Edge> path = paths.getEdges();
        for (int i = 0; i < path.size(); i++) {
            Edge e = path.get(i);
            Set<Property> localprops = localedgesmap.get(e);
            Object remainbw = new Object();
            for (Iterator it = localprops.iterator(); it.hasNext();) {
                Object itnext = it.next();
                if (itnext instanceof Bandwidth) {
                    remainbw = itnext;
                } else
                    continue;
            }
            long modifiedbw;
            if (minusOrPlus == true)
                modifiedbw = (long) (((Bandwidth) remainbw).getValue()
                    - bw * Math.pow(10, 6));
            else modifiedbw = (long) (((Bandwidth) remainbw).getValue()
                    + bw * Math.pow(10, 6));
            localprops.remove(remainbw);
            if (modifiedbw != 0)
                localprops.add(new Bandwidth(modifiedbw));
            log.info("show changed props{}", localprops);
            localedgesmap.put(e, localprops);
        }
    }

    private void deletePath(Path path){
        List<Edge> pathedges = path.getEdges();
        for (Iterator<Edge> i = pathedges.iterator(); i.hasNext();) {
            Edge pathedge = i.next();
            updateTopo(pathedge,(short) 0, UpdateType.REMOVED);
        }
    }

    public void recoverPath(Path path){
        List<Edge> pathedges = path.getEdges();
        for (Iterator<Edge> i = pathedges.iterator(); i.hasNext();) {
            Edge pathedge = i.next();
            updateTopo(pathedge,(short) 0, UpdateType.ADDED);
        }
    }

    /*public void recoverBw(Path path){
        bwUpdate(path,pathflowbwmap.get(path),false);
    }*/

    public Double pathTime(Path path,int mb){
        double time = 0;
        List<Edge> links = path.getEdges();
        for (Edge link : links) {
            Set<Property> localprops = localedgesmap.get(link);
            for (Iterator it = localprops.iterator(); it.hasNext();) {
                Object currentbw = it.next();
                if (currentbw instanceof Bandwidth) {
                    time += ((double)mb)/(((Bandwidth) currentbw).getValue());
                }
            }
        }
        return time;
    }

    public Path randomPath(final Node src, final Node dst,short bw,int eermb){
        ArrayList<Path> paths = nPath(src,dst,bw);
        short N = 4;
        short n = 0;
        if(paths == null) return null;
        if(paths.isEmpty()) return null;
        if(paths.size() == 1)  n=0;
        else{
            /*if(paths.size()<N) n = (short) random.nextInt(paths.size()-1);
            else n = (short) random.nextInt(N-1);*/
            if(paths.size()<N){
                Double time[] = new Double[paths.size()];
                for (short i = 0;i<paths.size();i++) {
                    time[i] = pathTime(paths.get(i),eermb);
                }
                for (short j = 1;j<paths.size();j++){
                    n=0;
                    if (time[n]>time[j]) n = j;
                }
            }
            else{
                Double time[] = new Double[N];
                for (short i = 0;i<N;i++) {
                    time[i] = pathTime(paths.get(i),eermb);
                }
                for (short j = 1;j<N;j++){
                    n=0;
                    if (time[n]>time[j]) n = j;
                }
            }
        }
        List<Edge> path = paths.get(n).getEdges();
        randomusedpath.add(paths.get(n));
        //pathflowbwmap.put(paths.get(n), bw);
        bwUpdate(paths.get(n), bw,true);
        return paths.get(n);
    }

    public ArrayList<Path> nPath(final Node src, final Node dst,short bw){
        short N = 4;
        ArrayList<Edge> CutEdgeSet[] = new ArrayList[N];
        ArrayList<Path> Paths = new ArrayList<Path>();
        //Graph<Node, Edge> topo = this.topologyBWAware.get(new Short((short)0));
        for(short i=0;i<N;i++){
            CutEdgeSet[i]= new ArrayList<Edge>();
        }
        for(short i= 0; i < N ; i++ ){
            if(i==0){
                Path path=getRoute(src,dst,bw);
                if (path == null) return null;
                Paths.add(path);
            } else{
                for(Iterator<Edge> m = CutEdgeSet[i-1].iterator(); m.hasNext(); ){
                    Edge cutedge= m.next();
                    //topo.removeEdge(cutedge);
                    updateTopo(cutedge, bw, UpdateType.REMOVED);
                }
                if (Paths.size()<i+1) break;
                else{
                    if(Paths.get(i-1)== null) break;
                    List<Edge> edges = (Paths.get(i-1)).getEdges();
                    for(Iterator<Edge> n = edges.iterator(); n.hasNext(); ){
                        Edge edge = n.next();
                        //NodeConnector edgesrc = edge.getTailNodeConnector();
                        //NodeConnector edgedst = edge.getHeadNodeConnector();
                        /*for( short jj= 0; jj < N ; jj++ ){
                            CutEdgeSet[i-1].add(edge);
                            if(CutEdgeSet[i-1].containsAll(CutEdgeSet[jj])) continue;
                        }*/
                        //CutEdgeSet[i-1].remove(edge);
                        //topo.removeEdge(edge);
                        updateTopo(edge, bw, UpdateType.REMOVED);
                        Path path2=getRoute(src,dst,bw);
                        //topo.addEdge(edge, edgesrc.getNode(), edgedst.getNode(), EdgeType.DIRECTED);
                        updateTopo(edge, bw, UpdateType.ADDED);
                        if (path2 == null) continue;
                        int newsize = path2.getEdges().size();
                        for(short k = i; k < N; k++ ){
                            if(Paths.size()<k+1){
                                    Paths.add(path2);
                                    CutEdgeSet[k].clear();
                                    CutEdgeSet[k].addAll(CutEdgeSet[i-1]);
                                    CutEdgeSet[k].add(edge);
                                    break;
                            }else {
                                if (Paths.get(k) == path2) break;
                                else{
                                    if(Paths.get(k).getEdges().size()> newsize){
                                        for(int kk= N-1; kk> k; kk-- ){
                                            Paths.set(kk, Paths.get(kk-1));
                                              CutEdgeSet[kk]= CutEdgeSet [kk-1];
                                        }
                                        Paths.set(k, path2);
                                        CutEdgeSet[k].clear();
                                        CutEdgeSet[k].addAll(CutEdgeSet[i-1]);
                                        CutEdgeSet[k].add(edge);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                for(Iterator<Edge> m = CutEdgeSet[i-1].iterator(); m.hasNext(); ){
                    Edge cutedge=m.next();
                    //NodeConnector cutedgesrc = cutedge.getTailNodeConnector();
                    //NodeConnector cutedgedst = cutedge.getHeadNodeConnector();
                    //topo.addEdge(cutedge,cutedgesrc.getNode(), cutedgedst.getNode(), EdgeType.DIRECTED);
                    updateTopo(cutedge, bw, UpdateType.ADDED);
                }
            }
        }
        return Paths;
    }

    public void initWeight(){
        Graph<Node, Edge> topo = this.topologyBWAware.get(new Short((short) 0));
        Collection<Edge> topoedges = topo.getEdges();
        Map<Edge, Number> LinkCostMap = new HashMap<Edge, Number>();
        for (Iterator<Edge> i = topoedges.iterator(); i.hasNext();) {
            Edge topoedge = i.next();
            LinkCostMap.put(topoedge,100);
        }
        this.initMaxThroughput(LinkCostMap);
        /*Path path = this.getWeightRoute(src,dst,(short) 0);
        List<Edge> pathedges = path.getEdges();
        for (Iterator<Edge> i = pathedges.iterator(); i.hasNext();) {
            Edge pathedge = i.next();
            NodeConnector tailNodeConnector = pathedge.getTailNodeConnector();
            Node tailnode = tailNodeConnector.getNode();
            Collection<Edge> tailnodeedges = topo.getIncidentEdges(tailnode);
            for (Iterator<Edge> m = tailnodeedges.iterator(); m.hasNext();){
                Edge tailnodeedge = m.next();
                LinkCostMap.put(tailnodeedge,1);
            }
            if (pathedge == pathedges.get(pathedges.size()-1)){
                NodeConnector headNodeConnector = pathedge.getHeadNodeConnector();
                Node headnode = headNodeConnector.getNode();
                Collection<Edge> headnodeedges = topo.getIncidentEdges(headnode);
                for (Iterator<Edge> m = headnodeedges.iterator(); m.hasNext();){
                    Edge headnodeedge = m.next();
                    LinkCostMap.put(headnodeedge,1);
                }
            }
        }
        this.deletePath(path);
        return path;*/
    }

    private void modifyWeight(Path path){
        Graph<Node, Edge> topo = this.topologyBWAware.get(new Short((short) 0));
        Map<Edge, Number> LinkCostMap = new HashMap<Edge, Number>();
        List<Edge> pathedges = path.getEdges();
        for (Iterator<Edge> i = pathedges.iterator(); i.hasNext();) {
            Edge pathedge = i.next();
            NodeConnector tailNodeConnector = pathedge.getTailNodeConnector();
            Node tailnode = tailNodeConnector.getNode();
            Collection<Edge> tailnodeedges = topo.getIncidentEdges(tailnode);
            for (Iterator<Edge> m = tailnodeedges.iterator(); m.hasNext();){
                Edge tailnodeedge = m.next();
                LinkCostMap.put(tailnodeedge,1);
            }
            if (pathedge == pathedges.get(pathedges.size()-1)){
                NodeConnector headNodeConnector = pathedge.getHeadNodeConnector();
                Node headnode = headNodeConnector.getNode();
                Collection<Edge> headnodeedges = topo.getIncidentEdges(headnode);
                for (Iterator<Edge> m = headnodeedges.iterator(); m.hasNext();){
                    Edge headnodeedge = m.next();
                    LinkCostMap.put(headnodeedge,1);
                }
            }
        }
        this.initMaxThroughput(LinkCostMap);
    }

    public Path getWeightPath(final Node src, final Node dst){
        Path path = this.getWeightRoute(src,dst,(short) 0);
        if (path == null) return null;
        this.modifyWeight(path);
        this.deletePath(path);
        if (path != null)
            weightusedpath.add(path);
        return path;
    }

    public synchronized Path getWeightRoute(final Node src, final Node dst,
            final Short Bw) {
        //DijkstraShortestPath<Node, Edge> spt = this.sptBWAware.get(Bw);
        if (mtp == null) {
            this.initWeight();
        }
        List<Edge> path;
        try {
            path = mtp.getPath(src, dst);
        } catch (IllegalArgumentException ie) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        Path res;
        try {
            res = new Path(path);
        } catch (ConstructionException e) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        return res;
    }

    /*public synchronized Path getBwRoute(final Node src, final Node dst,
            final Short Bw) {
        DijkstraShortestPath<Node, Edge> spt = this.sptBWAware.get(Bw);
        if (spt == null) {
            return null;
        }
        List<Edge> path;
        try {
            path = spt.getPath(src, dst);
            usedpath.addAll(path);
            bwUpdate(path, Bw);
        } catch (IllegalArgumentException ie) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        Path res;
        try {
            res = new Path(path);
        } catch (ConstructionException e) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        return res;
    }*/

    // 被Activator.java调用,设置routingAware属性
    public void setListenRoutingUpdates(final IListenRoutingUpdates i) {
        if (this.routingAware == null) {
            this.routingAware = new HashSet<IListenRoutingUpdates>();
        }
        if (this.routingAware != null) {
            log.debug("Adding routingAware listener: {}", i);
            this.routingAware.add(i);
        }
    }

    public void unsetListenRoutingUpdates(final IListenRoutingUpdates i) {
        if (this.routingAware == null) {
            return;
        }
        log.debug("Removing routingAware listener");
        this.routingAware.remove(i);
        if (this.routingAware.isEmpty()) {
            // We don't have any listener lets dereference
            this.routingAware = null;
        }
    }

    @Override
    public synchronized void initMaxThroughput(
            final Map<Edge, Number> EdgeWeightMap) {
        if (mtp != null) {
            log.error("Max Throughput Dijkstra is already enabled!");
            return;
        }
        Transformer<Edge, ? extends Number> mtTransformer = null;
        if (EdgeWeightMap == null) {
            mtTransformer = new Transformer<Edge, Double>() {
                @Override
                public Double transform(Edge e) {
                    if (switchManager == null) {
                        log.error("switchManager is null");
                        return (double) -1;
                    }
                    NodeConnector srcNC = e.getTailNodeConnector();
                    NodeConnector dstNC = e.getHeadNodeConnector();
                    if ((srcNC == null) || (dstNC == null)) {
                        log.error("srcNC:{} or dstNC:{} is null", srcNC, dstNC);
                        return (double) -1;
                    }
                    Bandwidth bwSrc = (Bandwidth) switchManager
                            .getNodeConnectorProp(srcNC,
                                    Bandwidth.BandwidthPropName);
                    Bandwidth bwDst = (Bandwidth) switchManager
                            .getNodeConnectorProp(dstNC,
                                    Bandwidth.BandwidthPropName);

                    long srcLinkSpeed = 0, dstLinkSpeed = 0;
                    if ((bwSrc == null)
                            || ((srcLinkSpeed = bwSrc.getValue()) == 0)) {
                        log.debug(
                                "srcNC: {} - Setting srcLinkSpeed to Default!",
                                srcNC);
                        srcLinkSpeed = DEFAULT_LINK_SPEED;
                    }

                    if ((bwDst == null)
                            || ((dstLinkSpeed = bwDst.getValue()) == 0)) {
                        log.debug(
                                "dstNC: {} - Setting dstLinkSpeed to Default!",
                                dstNC);
                        dstLinkSpeed = DEFAULT_LINK_SPEED;
                    }

                    // TODO: revisit the logic below with the real use case in
                    // mind
                    // For now we assume the throughput to be the speed of the
                    // link itself
                    // this kind of logic require information that should be
                    // polled by statistic manager and are not yet available,
                    // also this service at the moment is not used, so to be
                    // revisited later on
                    long avlSrcThruPut = srcLinkSpeed;
                    long avlDstThruPut = dstLinkSpeed;

                    // Use lower of the 2 available throughput as the available
                    // throughput
                    long avlThruPut = avlSrcThruPut < avlDstThruPut
                            ? avlSrcThruPut : avlDstThruPut;

                    if (avlThruPut <= 0) {
                        log.debug("Edge {}: Available Throughput {} <= 0!", e,
                                avlThruPut);
                        return (double) -1;
                    }
                    return (double) (Bandwidth.BW1Pbps / avlThruPut);
                }
            };
        } else {
            mtTransformer = new Transformer<Edge, Number>() {
                @Override
                public Number transform(Edge e) {
                    return EdgeWeightMap.get(e);
                }
            };
        }
        Short baseBW = Short.valueOf((short) 0);
        // Initialize mtp also using the default topo
        Graph<Node, Edge> g = this.topologyBWAware.get(baseBW);
        if (g == null) {
            log.error("Default Topology Graph is null");
            return;
        }
        mtp = new DijkstraShortestPath<Node, Edge>(g, mtTransformer);
    }

    @Override
    public Path getRoute(final Node src, final Node dst) {
        if ((src == null) || (dst == null)) {
            return null;
        }
        return getRoute(src, dst, (short) 0);
    }

    @Override
    public synchronized Path getMaxThroughputRoute(Node src, Node dst) {
        if (mtp == null) {
            log.error("Max Throughput Path Calculation Uninitialized!");
            return null;
        }

        List<Edge> path;
        try {
            path = mtp.getMaxThroughputPath(src, dst);
        } catch (IllegalArgumentException ie) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        Path res;
        try {
            res = new Path(path);
        } catch (ConstructionException e) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        return res;
    }

    @Override
    public synchronized Path getRoute(final Node src, final Node dst,
            final Short Bw) {
        DijkstraShortestPath<Node, Edge> spt = this.sptBWAware.get(Bw);
        if (spt == null) {
            return null;
        }
        List<Edge> path;
        try {
            path = spt.getPath(src, dst);
            /*if (usedPath!=null)
                usedPath.clear();
            usedPath=path;
            for (int i =0; i < usedPath.size(); i++) {
                Edge e = usedPath.get(i);
                Bandwidth bw = new Bandwidth(0);
                boolean newEdge = false;
                Short baseBW = Short.valueOf((short) 0);
                // Update base topo
                newEdge = !updateTopo(e, baseBW, updateType);
                if (newEdge == true) {
                    if(bw.getValue() != baseBW) {
                        // Update BW topo
                        updateTopo(e,(short) bw.getValue(), UpdateType.REMOVED);
                    }
                }
            }*/
        } catch (IllegalArgumentException ie) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        Path res;
        try {
            res = new Path(path);
        } catch (ConstructionException e) {
            log.debug("A vertex is yet not known between {} {}", src, dst);
            return null;
        }
        return res;
    }

    @Override
    public synchronized void clear() {
        DijkstraShortestPath<Node, Edge> spt;
        for (Short bw : this.sptBWAware.keySet()) {
            spt = this.sptBWAware.get(bw);
            if (spt != null) {
                spt.reset();
            }
        }
        clearMaxThroughput();
    }

    @Override
    public synchronized void clearMaxThroughput() {
        if (mtp != null) {
            mtp.reset(); // reset max throughput path
        }
    }

    // 被下面的edgeUpdate调用
    // 提供edge（键），bw（带宽），用这个方法，改变topo变量。
    // 先用bw，判断topo是否已经存在，不存在，则创建，有则看updatetype，是要添加、修改，还是删除。
    @SuppressWarnings({ "unchecked" })
    private synchronized boolean updateTopo(Edge edge, Short bw,
            UpdateType type) {
        //log.info("Routing updateTopo() is called");
        Graph<Node, Edge> topo = this.topologyBWAware.get(bw); //返回的是一个指针，所以修改topo后，topologyBWAware中数据也会改变
        DijkstraShortestPath<Node, Edge> spt = this.sptBWAware.get(bw);
        boolean edgePresentInGraph = false;
        Short baseBW = Short.valueOf((short) 0);

        if (topo == null) {
            // Create topology for this BW
            Graph<Node, Edge> g = new SparseMultigraph();
            this.topologyBWAware.put(bw, g);
            topo = this.topologyBWAware.get(bw);
            // spt由g得到
            this.sptBWAware.put(bw, new DijkstraShortestPath(g));
            spt = this.sptBWAware.get(bw);
        }

        if (topo != null) {
            NodeConnector src = edge.getTailNodeConnector();
            NodeConnector dst = edge.getHeadNodeConnector();
            if (spt == null) {
                // spt由topo得到
                spt = new DijkstraShortestPath(topo);
                // 添加到属性sptBWAware中
                this.sptBWAware.put(bw, spt);
            }

            switch (type) {
                case ADDED:
                    // Make sure the vertex are there before adding the edge
                    // 这个类应该自带数据结构，添加node和edge会有一些隐藏的判断
                    topo.addVertex(src.getNode());
                    topo.addVertex(dst.getNode());
                    // Add the link between
                    edgePresentInGraph = topo.containsEdge(edge);
                    // 判断图中是否已包含此边
                    if (edgePresentInGraph == false) {
                        try {
                            // 根据给的edge获得它的src和dst，再重新组成边，这样做的目的是消除类（edge）的不兼容性（不同程序员可能定义的edge不同）
                            // 图中键的类型为有向的
                            topo.addEdge(new Edge(src, dst), src.getNode(),
                                    dst.getNode(), EdgeType.DIRECTED);
                        } catch (final ConstructionException e) {
                            log.error("", e);
                            return edgePresentInGraph;
                        }
                    }
                case CHANGED:
                    // Mainly raised only on properties update, so not really
                    // useful
                    // in this case
                    break;
                case REMOVED:
                    // Remove the edge
                    try {
                        topo.removeEdge(new Edge(src, dst));
                    } catch (final ConstructionException e) {
                        log.error("", e);
                        return edgePresentInGraph;
                    }

                    // If the src and dst vertex don't have incoming or
                    // outgoing links we can get ride of them
                    if (topo.containsVertex(src.getNode())
                            && (topo.inDegree(src.getNode()) == 0)
                            && (topo.outDegree(src.getNode()) == 0)) {
                        log.debug("Removing vertex {}", src);
                        topo.removeVertex(src.getNode());
                    }

                    if (topo.containsVertex(dst.getNode())
                            && (topo.inDegree(dst.getNode()) == 0)
                            && (topo.outDegree(dst.getNode()) == 0)) {
                        log.debug("Removing vertex {}", dst);
                        topo.removeVertex(dst.getNode());
                    }
                    break;
            }
            spt.reset();
            if (bw.equals(baseBW)) {
                clearMaxThroughput();
            }
        } else {
            log.error("Cannot find topology for BW {} this is unexpected!", bw);
        }
        return edgePresentInGraph;
    }

    // 被下面的edgeUpdate调用
    // 做了一系列判断之后，调用了updateTopo方法，应该是用每一条边来updateTopo吧
    private boolean edgeUpdate(Edge e, UpdateType type, Set<Property> props,
            boolean local) {
        //log.info("Routing edgeUpdate_single() is called");
        String srcType = null;
        String dstType = null;

        log.trace("Got an edgeUpdate: {} props: {} update type: {} local: {}",
                new Object[] { e, props, type, local });

        if ((e == null) || (type == null)) {
            log.error("Edge or Update type are null!");
            return false;
        } else {
            srcType = e.getTailNodeConnector().getType();
            dstType = e.getHeadNodeConnector().getType();

            if (srcType.equals(NodeConnector.NodeConnectorIDType.PRODUCTION)) {
                log.debug("Skip updates for {}", e);
                return false;
            }

            if (dstType.equals(NodeConnector.NodeConnectorIDType.PRODUCTION)) {
                log.debug("Skip updates for {}", e);
                return false;
            }
        }

        Bandwidth bw = new Bandwidth(0);
        boolean newEdge = false;
        if (props != null) {
            props.remove(bw);
        }

        Short baseBW = Short.valueOf((short) 0);
        // Update base topo
        newEdge = !updateTopo(e, baseBW, type);
        //log.info("baseBW");
        if (newEdge == true) {
            if (bw.getValue() != baseBW) {
                // Update BW topo
                updateTopo(e, (short) bw.getValue(), type);
                //log.info("bw");
            }
        }
        return newEdge;
    }

    // 被下面的start()调用
    // 调用上面的类edgeUpdate方法，TopoEdgeUpdate中装有边的信息
    @Override
    public void edgeUpdate(List<TopoEdgeUpdate> topoedgeupdateList) {
        //log.info("Routing edgeUpdate_list() is called");
        log.trace("Start of a Bulk EdgeUpdate with " + topoedgeupdateList.size()
                + " elements");
        boolean callListeners = false;
        for (int i = 0; i < topoedgeupdateList.size(); i++) {
            // 遍历topoedgeupdateList，取出每条边的信息
            Edge e = topoedgeupdateList.get(i).getEdge();
            Set<Property> p = topoedgeupdateList.get(i).getProperty();
            UpdateType type = topoedgeupdateList.get(i).getUpdateType();
            boolean isLocal = topoedgeupdateList.get(i).isLocal();
            // 对每条边的信息更新
            if ((edgeUpdate(e, type, p, isLocal)) && (!callListeners)) {
                callListeners = true;
            }
        }

        // The routing listeners should only be called on the coordinator, to
        // avoid multiple controller cluster nodes to actually do the
        // recalculation when only one need to react
        boolean amICoordinator = true;
        if (this.clusterContainerService != null) {
            amICoordinator = this.clusterContainerService.amICoordinator();
        }
        if ((callListeners) && (this.routingAware != null) && amICoordinator) {
            log.trace("Calling the routing listeners");
            for (IListenRoutingUpdates ra : this.routingAware) {
                try {
                    ra.recalculateDone();
                } catch (Exception ex) {
                    log.error("Exception on routingAware listener call", ex);
                }
            }
        }
        log.trace("End of a Bulk EdgeUpdate");
    }

    /**
     * Function called by the dependency manager when all the required
     * dependencies are satisfied
     *
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void init() {
        log.info("Routing init() is called");
        this.topologyBWAware = new ConcurrentHashMap<Short, Graph<Node, Edge>>(); // ConcurrentHashMap实现了ConcurrentMap接口
        this.sptBWAware = new ConcurrentHashMap<Short, DijkstraShortestPath<Node, Edge>>();
        // Now create the default topology, which doesn't consider the
        // BW, also create the corresponding Dijkstra calculation
        Graph<Node, Edge> g = new SparseMultigraph();
        Short sZero = Short.valueOf((short) 0);
        this.topologyBWAware.put(sZero, g);
        this.sptBWAware.put(sZero, new DijkstraShortestPath(g));
        // Topologies for other BW will be added on a needed base
    }

    /**
     * Function called by the dependency manager when at least one dependency
     * become unsatisfied or when the component is shutting down because for
     * example bundle is being stopped.
     *
     */
    void destroy() {
        log.debug("Routing destroy() is called");
    }

    /**
     * Function called by dependency manager after "init ()" is called and after
     * the services provided by the class are registered in the service registry
     *
     */

    // 调用edgeUpdate方法
    // 从topologyManager获得Edge和其Property信息（存在map中），将这两个信息转化为装在类TopoEdgeUpdate中
    void start() {
        log.info("Routing start() is called");
        // build the routing database from the topology if it exists.
        Map<Edge, Set<Property>> edges = topologyManager.getEdges();
        if (edges.isEmpty()) {
            return;
        }
        List<TopoEdgeUpdate> topoedgeupdateList = new ArrayList<TopoEdgeUpdate>();
        log.debug("Creating routing database from the topology");
        for (Iterator<Map.Entry<Edge, Set<Property>>> i = edges.entrySet()
                .iterator(); i.hasNext();) {
            Map.Entry<Edge, Set<Property>> entry = i.next();
            Edge e = entry.getKey();
            Set<Property> props = entry.getValue();
            TopoEdgeUpdate topoedgeupdate = new TopoEdgeUpdate(e, props,
                    UpdateType.ADDED);
            topoedgeupdateList.add(topoedgeupdate);
        }
        edgeUpdate(topoedgeupdateList);
    }

    /**
     * Function called by the dependency manager before the services exported by
     * the component are unregistered, this will be followed by a "destroy ()"
     * calls
     *
     */
    public void stop() {
        log.debug("Routing stop() is called");
    }

    public void setSwitchManager(ISwitchManager switchManager) {
        this.switchManager = switchManager;
    }

    public void unsetSwitchManager(ISwitchManager switchManager) {
        if (this.switchManager == switchManager) {
            this.switchManager = null;
        }
    }

    public void setTopologyManager(ITopologyManager tm) {
        this.topologyManager = tm;
    }

    public void unsetTopologyManager(ITopologyManager tm) {
        if (this.topologyManager == tm) {
            this.topologyManager = null;
        }
    }

    void setClusterContainerService(IClusterContainerServices s) {
        log.debug("Cluster Service set");
        this.clusterContainerService = s;
    }

    void unsetClusterContainerService(IClusterContainerServices s) {
        if (this.clusterContainerService == s) {
            log.debug("Cluster Service removed!");
            this.clusterContainerService = null;
        }
    }

    @Override
    public void edgeOverUtilized(Edge edge) {
        // TODO Auto-generated method stub

    }

    @Override
    public void edgeUtilBackToNormal(Edge edge) {
        // TODO Auto-generated method stub

    }

    public ConcurrentHashMap<FlowEntry,Path> getFirstFEPathMap(){
        ConcurrentHashMap<FlowEntry,Path> FlowEntryPathMap = new ConcurrentHashMap<FlowEntry,Path>();
        FlowEntryPathMap.putAll(firstFEPathMap);
        return FlowEntryPathMap;
    }

    public Vector<Path> getWeightUsedPath(){
        Vector<Path> weightpaths = new Vector<Path> ();
        weightpaths.addAll(weightusedpath);
        return weightpaths;
    }

    public LinkedList<Path> getRandomUsedPath(){
        LinkedList<Path> randompaths = new LinkedList<Path> ();
        randompaths.addAll(randomusedpath);
        return randompaths;
    }

    public ArrayList<ArrayList> getDropList(){
        ArrayList<ArrayList> dropList = new ArrayList<ArrayList> ();
        dropList.addAll(this.dropVector);
        return dropList;
    }
}
