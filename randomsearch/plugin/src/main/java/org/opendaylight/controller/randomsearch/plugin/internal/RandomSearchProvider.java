package org.opendaylight.controller.randomsearch.plugin.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.controller.EER.plugin.internal.EERImpl;
import org.opendaylight.controller.forwardingrulesmanager.FlowEntry;
import org.opendaylight.controller.forwardingrulesmanager.IForwardingRulesManager;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.myrouting.MyRoutingImpl;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Drop;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.Path;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.reader.FlowOnNode;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.IPProtocols;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.statisticsmanager.IStatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EERService;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RandomSearchProvider extends AbstractBindingAwareProvider {
    private static Logger log = LoggerFactory.getLogger(RandomSearchProvider.class);
    private ConsumerContext session;
    IStatisticsManager statisticsmanager;
    IRouting routing;
    EERService eer;
    MyRoutingImpl mri;
    IForwardingRulesManager frm;
    EERImpl eerimpl;

    //ConcurrentHashMap<Path,Short> weightpathsmap;
    //ConcurrentHashMap<FlowEntry,Path> searchFirstFEPathMap;
    //Set<Path> weightpaths;
    //LinkedList<Path> weightfinishedpaths = new LinkedList<Path>();
    //LinkedList<Path> randompaths;
    //LinkedList<Path> randomfinishedpaths = new LinkedList<Path>();
    //ArrayList<ArrayList> dropList;
    //FlowOnNode flowonnode;
    //HostNodeConnector srcNodeC;
    //HostNodeConnector dstNodeC;
    //short dstPort;
    //public ArrayList dropList;
    //public HashMap<Path,LinkedList<FlowEntry>> pathflowmap;
    //public Set<Path> none = Collections.synchronizedSet(new HashSet<Path>());

    private final static Logger LOG = LoggerFactory
            .getLogger(RandomSearchProvider.class);

    public RandomSearchProvider() {

    }

    @Override
    public Collection<? extends RpcService> getImplementations() {
        return null;
    }

    @Override
    public Collection<? extends ProviderFunctionality> getFunctionality() {
        return null;
    }


    @Override
    public void onSessionInitiated(ProviderContext session) {
        this.session = session;
        if (routing == null) routing = (IRouting) ServiceHelper.getGlobalInstance(IRouting.class,this);
        if (mri == null) mri = (MyRoutingImpl) routing;
        if(statisticsmanager == null) statisticsmanager = (IStatisticsManager) ServiceHelper.getGlobalInstance(IStatisticsManager.class, this);
        if (frm == null) frm = (IForwardingRulesManager) ServiceHelper.getGlobalInstance(IForwardingRulesManager.class, this);
        /*weightpaths= mri.weightusedpath;
        randompaths= mri.randomusedpath;
        dropList = mri.dropList;*/
        if (eer == null) {
            eer = (EERService) this.session.getRpcService(EERService.class);//获得PingService的实例，即PingImpl.java
            //eerimpl= (EERImpl) eer;
        }

        while(true){
            /*if (routing == null) routing = (IRouting) ServiceHelper.getGlobalInstance(IRouting.class,this);
            if (mri == null) mri = (MyRoutingImpl) routing;
            if(statisticsmanager == null) statisticsmanager = (IStatisticsManager) ServiceHelper.getGlobalInstance(IStatisticsManager.class, this);
            if (frm == null) frm = (IForwardingRulesManager) ServiceHelper.getGlobalInstance(IForwardingRulesManager.class, this);
            if (mri != null){*/
            /*if (mri.weightusedpath != null){
                weightpathsmap = mri.getWeightUsedPath();
                weightpaths = weightpathsmap.keySet();
                for (Iterator<Path> it = weightpaths.iterator(); it.hasNext();){//迭代已经部署的路径
                    Path path = it.next();
                    while(mri.pathflowmap == null){}
                    LinkedList<FlowEntry> fes = mri.pathflowmap.get(path);//获得路径对应的流表项
                    if (fes != null){
                        Node startnode = path.getStartNode();
                        List<FlowOnNode> flowsonnode = statisticsmanager.getFlows(startnode);//获得与信源相连交换机的流表项统计信息
                        for (Iterator<FlowOnNode> i = flowsonnode.iterator(); i.hasNext();){
                            flowonnode = i.next();
                            if (fes.getFirst().getFlow().equals(flowonnode.getFlow())){//判断是否是路径对应的流表项
                                long packagenum = flowonnode.getPacketCount();
                                try{
                                    Thread thread = Thread.currentThread();
                                    thread.sleep(100);
                                }catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                long newpackagenum = flowonnode.getPacketCount();
                                if(packagenum == newpackagenum){//判断流是否结束
                                    //weightfinishedpaths.add(path);
                                    mri.weightusedpath.remove(path);//删除已安装路径统计
                                    log.info("the active pathnum is {}",mri.weightusedpath.size());
                                    mri.recoverPath(path);//恢复拓扑路径
                                    log.info("recovered path : {}" ,path);
                                    for (Iterator<FlowEntry> n = fes.iterator(); n.hasNext();){
                                        FlowEntry fe = n.next();
                                        frm.uninstallFlowEntry(fe);
                                    }
                                    mri.pathflowmap.remove(path);//流结束，删除流与flowentry的映射
                                    log.info("the active flownum is {}",mri.pathflowmap.size());
                                }
                            }
                        }
                    }
                }
            }*/



            if(mri.firstFEPathMap != null){
                ConcurrentHashMap<FlowEntry,Path> searchFirstFEPathMap = mri.getFirstFEPathMap();
                for (Iterator<Map.Entry<FlowEntry,Path>> it = searchFirstFEPathMap.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<FlowEntry,Path> entry = it.next();
                    FlowEntry flowentries = entry.getKey();
                    Path path = entry.getValue();
                    List<FlowOnNode> flowsonnode = statisticsmanager.getFlows(path.getStartNode());
                    for (Iterator<FlowOnNode> i = flowsonnode.iterator(); i.hasNext();){
                        FlowOnNode flowonnode = i.next();
                        if (flowentries.getFlow().equals(flowonnode.getFlow())){//判断是否是路径对应的流表项
                            long packagenum = flowonnode.getPacketCount();
                            try{
                                Thread thread = Thread.currentThread();
                                thread.sleep(500);
                            }catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            long newpackagenum = flowonnode.getPacketCount();
                            if(packagenum == newpackagenum){
                                if ((mri.weightusedpath != null)&&(!mri.weightusedpath.isEmpty())){
                                    mri.weightusedpath.remove(path);//删除已安装路径统计
                                    log.info("the active pathnum is {}",mri.weightusedpath.size());
                                    mri.recoverPath(path);//恢复拓扑路径
                                    log.info("recovered path : {}" ,path);
                                }
                                if ((mri.randomusedpath != null)&&(!mri.randomusedpath.isEmpty())){
                                    mri.bwUpdate(path,mri.firstFEBwMap.get(flowentries),false);//恢复带宽
                                    log.info("the addedbwpath is {}",path);
                                    mri.randomusedpath.remove(path);//delete the usud path
                                    log.info("the active pathbwnum is {}",mri.randomusedpath.size());
                                    mri.disOverLapFlowEntry.remove(flowentries);//delete the finished effective flow
                                    mri.disOverLapFlowEntry = deleteEffectiveFlowEntry(mri.disOverLapFlowEntry,flowentries);
                                }
                                mri.firstFEPathMap.remove(flowentries);
                                log.info("the active flowentrynum is {}",mri.firstFEPathMap.size());
                            }
                        }
                    }
                }
            }

            if(mri.dropVector != null){
                ArrayList<ArrayList> dropList = mri.getDropList();
                if (dropList != null&&!dropList.isEmpty()){//判断是否有阻塞的流
                    for (Iterator<ArrayList> it = dropList.iterator(); it.hasNext();){
                        ArrayList dl = it.next();
                        boolean bwOrWeight = (boolean) dl.get(0);
                        HostNodeConnector srcNodeC = (HostNodeConnector) dl.get(1);
                        HostNodeConnector dstNodeC = (HostNodeConnector) dl.get(2);
                        short dstPort = (short) dl.get(3);
                        int eermb = (int) dl.get(4);
                        int eerms = (int) dl.get(5);
                        short bw = (short) (eermb*1000*8/eerms);
                      //重新判断是否有路径存在
                        Path newres;
                        if (bwOrWeight == false){
                            newres = mri.getWeightPath(srcNodeC.getnodeconnectorNode(), dstNodeC.getnodeconnectorNode());
                        }
                        else{
                            mri.creatBwTopo(bw);
                            newres = mri.randomPath(srcNodeC.getnodeconnectorNode(), dstNodeC.getnodeconnectorNode(),bw,eermb);
                        }
                        if (newres != null){
                            log.info("the active pathnum plus one");
                            List<Edge> links = newres.getEdges();
                            if (links == null)
                                log.info("Look up failure: no edge avaiable");

                            LinkedList<FlowEntry> flowentries = new LinkedList<FlowEntry>();
                            for (Edge link : links) {
                                if (link == null) {
                                    //log.error("Could not retrieve the Link");
                                    // TODO: should we keep going?
                                    continue;
                                }
                                flowentries.add(updatePerHostRuleInSW(srcNodeC, dstNodeC, (short) 0, dstPort, link, 0, 0,true));
                            }
                            mri.dropVector.remove(dl);
                            log.info("the dropList minus one");
                            FlowEntry firstFE = flowentries.getFirst();
                            mri.firstFEPathMap.put(firstFE,newres);
                            log.info("the active flowEntry plus one");
                            mri.firstFEsMap.put(firstFE, flowentries);
                            if (bwOrWeight == true) {
                                mri.firstFEBwMap.put(firstFE,(short) (eermb*1000*8/eerms));
                                log.info("the eerflowentrybwmap size is {}",mri.firstFEBwMap.size());
                                mri.disOverLapFlowEntry = refreshEffectiveFlowEntry(mri.disOverLapFlowEntry,firstFE);
                            }
                        }
                    }
                }
            }

            /*if (mri.randomusedpath != null ){
                randompaths = mri.getRandomUsedPath();
                for (Iterator<Path> it = randompaths.iterator(); it.hasNext();){
                    Path path = it.next();
                    while(mri.pathflowmap == null){}
                    LinkedList<FlowEntry> fes = mri.pathflowmap.get(path);//获得路径对应的流表项
                    if (fes != null){
                        Node startnode = path.getStartNode();
                        List<FlowOnNode> flowsonnode = statisticsmanager.getFlows(startnode);
                        for (Iterator<FlowOnNode> i = flowsonnode.iterator(); i.hasNext();){
                            flowonnode = i.next();
                            if (fes.getFirst().getFlow().equals(flowonnode.getFlow())){
                                long packagenum = flowonnode.getPacketCount();
                                try{
                                    Thread thread = Thread.currentThread();
                                    thread.sleep(100);
                                }catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                long newpackagenum = flowonnode.getPacketCount();
                                if(packagenum == newpackagenum){
                                    //randomfinishedpaths.add(path);
                                    mri.recoverBw(path);
                                    log.info("the addedbwpath is {}",path);
                                    mri.randomusedpath.remove(path);
                                    mri.pathflowbwmap.remove(path);  //删除路径与带宽的映射
                                    //LinkedList<FlowEntry> fes = mri.pathflowmap.get(path);  //eer中路径与flowentry的映射
                                    for (Iterator<FlowEntry> n = fes.iterator(); n.hasNext();){
                                        FlowEntry fe = n.next();
                                        frm.uninstallFlowEntry(fe);
                                    }
                                    mri.pathflowmap.remove(path);
                                    //it.remove();
                                }
                            }
                        }
                    }
                }
                //randompaths.removeAll(randomfinishedpaths);
                //randomfinishedpaths.clear();
            }*/
        }
    }

    @Override
    protected void startImpl(BundleContext context) {
    }

    private FlowEntry updatePerHostRuleInSW(HostNodeConnector srcHost,
            HostNodeConnector dstHost, short srcPort, short dstPort, Edge link,
            int eerms,int eermb,boolean modifyOrDelFlow) {
        Match match = new Match();
        List<Action> actions = new ArrayList<Action>();

        match.setField(MatchType.DL_TYPE, EtherTypes.IPv4.shortValue());
        match.setField(MatchType.NW_DST, dstHost.getNetworkAddress());
        match.setField(MatchType.NW_SRC, srcHost.getNetworkAddress());
        match.setField(MatchType.NW_TOS, 4);
        match.setField(MatchType.NW_PROTO, IPProtocols.TCP.byteValue());
        match.setField(MatchType.TP_DST, dstPort);
        //match.setField(MatchType.TP_SRC, srcPort);

        //action改为output
        /*if(outputOrDrop == true)
            actions.add(new Output(link.getTailNodeConnector()));
        else actions.add(new Drop());*/
        actions.add(new Output(link.getTailNodeConnector()));

        Flow flow = new Flow(match, actions);
        flow.setIdleTimeout((short) 0);
        flow.setHardTimeout((short) 0);
        flow.setPriority((short) 5);
        Node currNode = link.getTailNodeConnector().getNode();
        String policyName = dstHost.getNetworkAddress().getHostAddress()
                + "/32";
        String flowName = "[" + dstHost.getNetworkAddress().getHostAddress()
                + "/32 on N " + currNode + "]";
        FlowEntry po = new FlowEntry(policyName, flowName, flow, currNode);
        // Populate the Policy field now
        frm = (IForwardingRulesManager) ServiceHelper.getGlobalInstance(
                IForwardingRulesManager.class, this);
        if (modifyOrDelFlow == true){
            Status poStatus = this.frm.modifyOrAddFlowEntry(po);
            if (!poStatus.isSuccess()) {
                log.error("Failed to install policy: " + po.getGroupName() + " ("
                        + poStatus.getDescription() + ")");
            } else {
                log.info("Successfully installed policy " + po.toString()
                        + " on switch " + currNode);
            }
        }else{
            Status poStatus = this.frm.uninstallFlowEntry(po);
            if (!poStatus.isSuccess()) {
                log.error("Failed to delete policy: " + po.getGroupName() + " ("
                        + poStatus.getDescription() + ")");
            } else {
                log.info("Successfully delete policy " + po.toString()
                        + " on switch " + currNode);
            }
        }
        return po;
    }

    public ArrayList<FlowEntry> orderActiveFlowEntry(ConcurrentHashMap<FlowEntry,Short> flowentryBwMap){
        ArrayList<FlowEntry> activefloworder = new ArrayList<FlowEntry>();
        activefloworder.addAll(flowentryBwMap.keySet());
        FlowEntry tmp;
        for(int i = 0;i<activefloworder.size()-1;i++){
            for (int j = 0;j<activefloworder.size()-1-i;j++){
                if (flowentryBwMap.get(activefloworder.get(j+1))>flowentryBwMap.get(activefloworder.get(j))){
                    tmp = activefloworder.get(j);
                    activefloworder.set(j, activefloworder.get(j+1));
                    activefloworder.set(j+1,tmp);
                }
            }
        }
        //activeFlowOrder = activefloworder;
        return activefloworder;
    }

    public boolean isOverlapped(Path path1,Path path2){
        List<Edge>edge1 = path1.getEdges();
        List<Edge>edge2 = path2.getEdges();
        for(Edge edge :edge2){
            if(edge1.contains(edge)) return false;
        }
        return true;
    }

    public void startOrPause(FlowEntry fe,Boolean flag){
        if (!flag){
            List<Action> actions = new ArrayList<Action>();
            actions.add(new Drop());
            Flow outputfl = fe.getFlow();
            Flow dropfl = new Flow(outputfl.getMatch(),actions);
            FlowEntry dropfe = new FlowEntry(fe.getGroupName(),fe.getFlowName(),dropfl,fe.getNode());
            this.frm.modifyOrAddFlowEntry(dropfe);
        }else{
            this.frm.modifyOrAddFlowEntry(fe);
        }
    }

    public synchronized Vector<FlowEntry> refreshEffectiveFlowEntry(Vector<FlowEntry> disOverLapFlowEntry,FlowEntry firstFE){
        ArrayList<FlowEntry> orderedactiveflow = orderActiveFlowEntry(mri.firstFEBwMap);//order the active flow list
        log.info("the size of the orderedactiveflow is {}",orderedactiveflow.size());
        Vector<FlowEntry> olddisOverLapFlowEntry = new Vector<FlowEntry>();
        olddisOverLapFlowEntry.addAll(disOverLapFlowEntry);
        if(!olddisOverLapFlowEntry.isEmpty()){
            //boolean flag = true;//if the added flow is effective
            short bw1 = mri.firstFEBwMap.get(firstFE);
            Path path1 = mri.firstFEPathMap.get(firstFE);
            for(Iterator<FlowEntry> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                FlowEntry fe = it.next();
                short bw2 = mri.firstFEBwMap.get(fe);
                if (bw1 <= bw2){
                    Path path2 = mri.firstFEPathMap.get(fe);
                    if(!isOverlapped(path1,path2)){//if the added flow is not effective
                        startOrPause(firstFE,false);//pause the flow
                        log.info("the added flow is not effective");
                        //flag = false;
                        return disOverLapFlowEntry;
                    }
                }
            }
            /*for(int j = 0;j<olddisOverLapFlowEntry.size();j++){
                short bw2 = flowentrybwmap.get(olddisOverLapFlowEntry.get(j));
                if (bw1 < bw2){
                    Path path2 =flowentrypathmap.get(olddisOverLapFlowEntry.get(j));
                    if(!isOverlapped(path1,path2)){//if the added flow is not effective
                        startOrPause(flowentries,false);//pause the flow
                        flag = false;
                        return olddisOverLapFlowEntry;
                    }
                }
            }*/

            olddisOverLapFlowEntry.add(firstFE);//add to the new effective list
            log.info("the added flow is effective");
            int i = orderedactiveflow.indexOf(firstFE);//consider the lower rate active flow
            int n = orderedactiveflow.size();
            if(i != n-1){//if the added flow is not the lowest one
                for(Iterator<FlowEntry> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                    FlowEntry fe = it.next();
                    short bw2 = mri.firstFEBwMap.get(fe);
                    if (bw1 > bw2){
                        startOrPause(fe,false);
                        it.remove();
                    }
                }
                /*for(int j = 0;j<olddisOverLapFlowEntry.size();j++){//first,pause all the lower effective flow and keep the faster effective flow
                    short bw2 = flowentrybwmap.get(olddisOverLapFlowEntry.get(j));
                    if (bw1 < bw2){
                        newdisOverLapFlowEntry.add(olddisOverLapFlowEntry.get(j));//add the faster rate effective flow
                    }else{
                        startOrPause(olddisOverLapFlowEntry.get(j),false);//pause all the lower effective flow
                    }
                }*/
                //Vector<LinkedList<FlowEntry>> newdisOverLapFlowEntry = new Vector<LinkedList<FlowEntry>>(olddisOverLapFlowEntry);
                for(i++;i<n;i++){//rejudge the lower rate active flow
                    FlowEntry flowEntry = orderedactiveflow.get(i);
                    short bw11 = mri.firstFEBwMap.get(flowEntry);
                    Path path11 = mri.firstFEPathMap.get(flowEntry);
                    for(int j = 0;j<olddisOverLapFlowEntry.size();j++){
                        short bw2 = mri.firstFEBwMap.get(olddisOverLapFlowEntry.get(j));
                        if (bw11 < bw2){
                            Path path2 = mri.firstFEPathMap.get(olddisOverLapFlowEntry.get(j));
                            if(isOverlapped(path11,path2)){
                                olddisOverLapFlowEntry.add(flowEntry);
                                startOrPause(flowEntry,true);
                            }
                        }
                    }
                    /*for(Iterator<LinkedList<FlowEntry>> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                        LinkedList<FlowEntry> fe = it.next();
                        short bw2 = flowentrybwmap.get(fe);
                        if (bw11 < bw2){
                            Path path2 =flowentrypathmap.get(fe);
                            if(isOverlapped(path11,path2)){
                                olddisOverLapFlowEntry.add(flowEntry);
                                startOrPause(flowEntry,true);
                            }
                        }
                    }*/
                    /*for(int j = 0;j<newdisOverLapFlowEntry.size();j++){
                        short bw2 = flowentrybwmap.get(newdisOverLapFlowEntry.get(j));
                        if (bw11 < bw2){
                            Path path2 =flowentrypathmap.get(newdisOverLapFlowEntry.get(j));
                            if(isOverlapped(path11,path2)){
                                newdisOverLapFlowEntry.add(flowEntry);
                                startOrPause(flowEntry,true);
                            }else{
                                startOrPause(flowEntry,false);
                            }
                        }
                    }*/
                }
                return olddisOverLapFlowEntry;
            }else{
                log.info("the added flow is the lowest active flow");
                return olddisOverLapFlowEntry;
            }
        }else{
            olddisOverLapFlowEntry.add(firstFE);
            return olddisOverLapFlowEntry;
        }

    }

    public Vector<FlowEntry> deleteEffectiveFlowEntry(Vector<FlowEntry> disOverLapFlowEntry,FlowEntry flowentries){
        ArrayList<FlowEntry> orderedactiveflow = orderActiveFlowEntry(mri.firstFEBwMap);//order the active flow list
        short bw1 = mri.firstFEBwMap.get(flowentries);
        int m = orderedactiveflow.indexOf(flowentries);//get the position of the delete flow
        mri.firstFEBwMap.remove(flowentries);//delete the finished flow
        orderedactiveflow.remove(m);//delete the finished flow
        if (m != orderedactiveflow.size()){
            Vector<FlowEntry> olddisOverLapFlowEntry = new Vector<FlowEntry>();
            olddisOverLapFlowEntry.addAll(disOverLapFlowEntry);
            //ArrayList<LinkedList<FlowEntry>> newdisOverLapFlowEntry = new ArrayList<LinkedList<FlowEntry>>();
            if(!olddisOverLapFlowEntry.isEmpty()){
                for(Iterator<FlowEntry> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                    FlowEntry fe = it.next();
                    if (mri.firstFEBwMap.get(fe) == null){
                        it.remove();
                        continue;
                    }
                    else{
                        short bw2 = mri.firstFEBwMap.get(fe);
                        if (bw1 > bw2){
                            startOrPause(fe,false);
                            it.remove();
                        }
                    }
                }
                /*for(int j = 0;j<olddisOverLapFlowEntry.size();j++){//pause all the lower effective flow first
                    short bw2 = mri.flowentrybwmap.get(olddisOverLapFlowEntry.get(j));
                    if (bw1 < bw2){
                        newdisOverLapFlowEntry.add(olddisOverLapFlowEntry.get(j));//add the faster rate effective flow
                    }else{
                        startOrPause(olddisOverLapFlowEntry.get(j),false);//pause all the lower effective flow
                    }
                }*/
                for(;m<orderedactiveflow.size();m++){//rejudge the lower rate active flow
                    FlowEntry flowEntry = orderedactiveflow.get(m);//get the lower rate flow
                    short bw11 = mri.firstFEBwMap.get(flowEntry);
                    Path path1 = mri.firstFEPathMap.get(flowEntry);
                    if(!olddisOverLapFlowEntry.isEmpty()){
                        for (int j = 0;j<olddisOverLapFlowEntry.size();j++){
                            short bw2 = mri.firstFEBwMap.get(olddisOverLapFlowEntry.get(j));
                            if (bw11 < bw2){
                                Path path2 =mri.firstFEPathMap.get(olddisOverLapFlowEntry.get(j));
                                if(isOverlapped(path1,path2)){
                                    olddisOverLapFlowEntry.add(flowEntry);
                                    startOrPause(flowEntry,true);
                                }
                            }
                        }
                        /*for(Iterator<LinkedList<FlowEntry>> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                            LinkedList<FlowEntry> fe = it.next();
                            short bw2 = mri.flowentrybwmap.get(fe);
                            if (bw11 < bw2){
                                Path path2 =flowentrypathmap.get(fe);
                                if(isOverlapped(path1,path2)){
                                    olddisOverLapFlowEntry.add(flowEntry);
                                    startOrPause(flowEntry,true);
                                }
                            }
                        }*/
                        /*for(int j = 0;j<newdisOverLapFlowEntry.size();j++){
                            short bw2 = mri.flowentrybwmap.get(newdisOverLapFlowEntry.get(j));
                            if (bw11 < bw2){
                                Path path2 =mri.flowentrypathmap.get(newdisOverLapFlowEntry.get(j));
                                if(isOverlapped(path1,path2)){
                                    newdisOverLapFlowEntry.add(flowEntry);
                                    startOrPause(flowEntry,true);
                                }else{
                                    startOrPause(flowEntry,false);
                                }
                            }
                        }*/
                   }else{
                       olddisOverLapFlowEntry.add(flowEntry);
                       startOrPause(flowEntry,true);
                   }
                }
                //mri.disOverLapFlowEntry = newdisOverLapFlowEntry;
                return olddisOverLapFlowEntry;
            }else{
                olddisOverLapFlowEntry.add(orderedactiveflow.get(m));
                for(m++;m<orderedactiveflow.size();m++){//rejudge the lower rate active flow
                    FlowEntry flowEntry = orderedactiveflow.get(m);//get the lower rate flow
                    short bw11 = mri.firstFEBwMap.get(flowEntry);
                    Path path1 = mri.firstFEPathMap.get(flowEntry);
                    for (int j = 0;j<olddisOverLapFlowEntry.size();j++){
                        short bw2 = mri.firstFEBwMap.get(olddisOverLapFlowEntry.get(j));
                        if (bw11 < bw2){
                            Path path2 =mri.firstFEPathMap.get(olddisOverLapFlowEntry.get(j));
                            if(isOverlapped(path1,path2)){
                                olddisOverLapFlowEntry.add(flowEntry);
                                startOrPause(flowEntry,true);
                            }
                        }
                    }
                    /*for(Iterator<FlowEntry> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                        FlowEntry fe = it.next();
                        short bw2 = mri.firstFEBwMap.get(fe);
                        if (bw11 < bw2){
                            Path path2 =mri.firstFEPathMap.get(fe);
                            if(isOverlapped(path1,path2)){
                                olddisOverLapFlowEntry.add(flowEntry);
                                startOrPause(flowEntry,true);
                            }
                        }
                    }*/
                    /*for(int j = 0;j<olddisOverLapFlowEntry.size();j++){
                        short bw2 = mri.flowentrybwmap.get(olddisOverLapFlowEntry.get(j));
                        if (bw11 < bw2){
                            Path path2 =mri.flowentrypathmap.get(olddisOverLapFlowEntry.get(j));
                            if(isOverlapped(path1,path2)){
                                olddisOverLapFlowEntry.add(flowEntry);
                                startOrPause(flowEntry,true);
                            }else{
                                startOrPause(flowEntry,false);
                            }
                        }
                    }*/
                }
                //mri.disOverLapFlowEntry = olddisOverLapFlowEntry;
                return olddisOverLapFlowEntry;
            }
        }
        return disOverLapFlowEntry;
    }

}