package org.opendaylight.controller.EER.plugin.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.Iterator;
import java.util.LinkedList;
//import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;
import java.util.Vector;

import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.controller.sal.core.Node;
//import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EERService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEERInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput.StartResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput.EndResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.util.concurrent.Futures;

//import javassist.bytecode.Descriptor.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
//import org.opendaylight.controller.sal.reader.FlowOnNode;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.IPProtocols;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
//import org.opendaylight.controller.switchmanager.IInventoryListener;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.sal.core.Edge;
//import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Path;
//import org.opendaylight.controller.sal.core.Property;
//import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.hosttracker.IfIptoHost;
//import org.opendaylight.controller.hosttracker.IfNewHostNotify;
import org.opendaylight.controller.hosttracker.hostAware.HostNodeConnector;
import org.opendaylight.controller.forwardingrulesmanager.FlowEntry;
import org.opendaylight.controller.forwardingrulesmanager.IForwardingRulesManager;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Drop;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.myrouting.MyRoutingImpl;
import org.opendaylight.controller.statisticsmanager.IStatisticsManager;
//import org.opendaylight.controller.statisticsmanager.internal.StatisticsManager;

public class EERImpl implements EERService {
    private static Logger log = LoggerFactory.getLogger(EERImpl.class);
    private static short DEFAULT_IPSWITCH_PRIORITY = 1;
    static final String FORWARDING_RULES_CACHE_NAME = "forwarding.ipswitch.rules";
    private IfIptoHost hostTracker;
    private IForwardingRulesManager frm;
    private ITopologyManager topologyManager;
    private IRouting routing;
    private ISwitchManager switchManager;
    private MyRoutingImpl mri;
    private IStatisticsManager statisticsmanager;
    public ConcurrentHashMap<FlowEntry,Path> firstFEPathMap = new ConcurrentHashMap<FlowEntry,Path>();
    public ConcurrentHashMap<FlowEntry,Short> firstFEBwMap = new ConcurrentHashMap<FlowEntry,Short>();
    public ConcurrentHashMap<FlowEntry,LinkedList<FlowEntry>> firstFEsMap = new ConcurrentHashMap<FlowEntry,LinkedList<FlowEntry>>();
    //LinkedList<FlowEntry> flowentries = new LinkedList<FlowEntry>();
    public ArrayList<ArrayList> dropVector = new ArrayList<ArrayList>();
    //public List dropList = Collections.synchronizedList(new ArrayList());
    //public HashSet<ArrayList> dropSet = new HashSet<ArrayList>();
    //RandomSearch rs;
    public HashSet<HashMap<Node,Double>> nodetime = new HashSet<HashMap<Node,Double>>();
    //public ArrayList<LinkedList<FlowEntry>> activeFlowOrder;
    //public ArrayList<LinkedList<FlowEntry>> disOverLapFlowEntry = new ArrayList<LinkedList<FlowEntry>>();

    private void updatePerHostRuleInSW(HostNodeConnector srcHost,
            HostNodeConnector dstHost, short srcPort, short dstPort, Node currNode,
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
        actions.add(new Drop());

        Flow flow = new Flow(match, actions);
        flow.setIdleTimeout((short) 0);
        flow.setHardTimeout((short) 0);
        flow.setPriority((short) 5);
        //Node currNode = link.getTailNodeConnector().getNode();
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

    /*private void deletePerHostRuleInSW(HostNodeConnector dstHost,
            HostNodeConnector srcHost, short dstPort, short srcPort, Edge link,
            boolean isAdd, short eerms,short eermb) {
        Match match = new Match();
        List<Action> actions = new ArrayList<Action>();

        match.setField(MatchType.DL_TYPE, EtherTypes.IPv4.shortValue());
        match.setField(MatchType.NW_DST, dstHost.getNetworkAddress());
        match.setField(MatchType.NW_SRC, srcHost.getNetworkAddress());
        match.setField(MatchType.NW_TOS, 4);
        match.setField(MatchType.NW_PROTO, IPProtocols.TCP.byteValue());
        match.setField(MatchType.TP_DST, dstPort);
        match.setField(MatchType.TP_SRC, srcPort);

        //action改为output
        actions.add(new Output(dstHost.getnodeConnector()));

        Flow flow = new Flow(match, actions);
        flow.setIdleTimeout((short) 0);
        flow.setHardTimeout((short) 0);
        flow.setPriority((short) 5);
        //flow.setPriority(DEFAULT_IPSWITCH_PRIORITY);
        Node currNode = link.getTailNodeConnector().getNode();
        String policyName = dstHost.getNetworkAddress().getHostAddress()
                + "/32";
        String flowName = "[" + dstHost.getNetworkAddress().getHostAddress()
                + "/32 on N " + currNode + "]";
        FlowEntry po = new FlowEntry(policyName, flowName, flow, currNode);
        // Populate the Policy field now
        frm = (IForwardingRulesManager) ServiceHelper.getGlobalInstance(
                IForwardingRulesManager.class, this);
        //删除路由
        Status poStatus = this.frm.uninstallFlowEntry(po);
        if (!poStatus.isSuccess()) {
            log.error("Failed to install policy: " + po.getGroupName() + " ("
                    + poStatus.getDescription() + ")");
        } else {
            log.info("Successfully installed policy " + po.toString()
                    + " on switch " + currNode);
        }
    }*/

    /*public void deleteRulesInSw(Path path){
        List<Edge> links = path.getEdges();
        for (Edge link : links) {
            if (link == null) {
                log.error("Could not retrieve the Link");
                // TODO: should we keep going?
                continue;
            }
            NodeConnector currHeadNode=link.getHeadNodeConnector();
            NodeConnector currTailNode=link.getTailNodeConnector();
            deletePerHostRuleInSW(currHeadNode, currHeadNode, dstPort, srcPort, link,true, eerms,eermb);
    }*/

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
        ArrayList<FlowEntry> orderedactiveflow = orderActiveFlowEntry(firstFEBwMap);//order the active flow list
        log.info("the size of the orderedactiveflow is {}",orderedactiveflow.size());
        Vector<FlowEntry> olddisOverLapFlowEntry = new Vector<FlowEntry>();
        olddisOverLapFlowEntry.addAll(disOverLapFlowEntry);
        if(!olddisOverLapFlowEntry.isEmpty()){
            //boolean flag = true;//if the added flow is effective
            short bw1 = firstFEBwMap.get(firstFE);
            Path path1 = firstFEPathMap.get(firstFE);
            for(Iterator<FlowEntry> it = olddisOverLapFlowEntry.iterator();it.hasNext();){
                FlowEntry fe = it.next();
                short bw2 = firstFEBwMap.get(fe);
                if (bw1 <= bw2){
                    Path path2 = firstFEPathMap.get(fe);
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
                    short bw2 = firstFEBwMap.get(fe);
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
                    short bw11 = firstFEBwMap.get(flowEntry);
                    Path path11 = firstFEPathMap.get(flowEntry);
                    for(int j = 0;j<olddisOverLapFlowEntry.size();j++){
                        short bw2 = firstFEBwMap.get(olddisOverLapFlowEntry.get(j));
                        if (bw11 < bw2){
                            Path path2 = firstFEPathMap.get(olddisOverLapFlowEntry.get(j));
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

    private boolean setPathRoute(InetAddress srcAddr, InetAddress dstAddr,
            short srcPort, short dstPort,int eerms,int eermb,boolean bwOrWeight) throws RuntimeException{
        hostTracker = (IfIptoHost) ServiceHelper.getGlobalInstance(
                IfIptoHost.class, this);
        //log.info("srcNodeC = hostTracker. hostFind...hostTracker exists? ");
        if (hostTracker == null) {
            log.info("No, return!");
            return false;
        }
        //log.info("Yes / {}:", hostTracker);

        HostNodeConnector srcNodeC = hostTracker.hostFind(srcAddr);
        HostNodeConnector dstNodeC = hostTracker.hostFind(dstAddr);
        //log.info("HostNode exists? {}", hostTracker == null);
        Node srcNode = srcNodeC.getnodeconnectorNode();
        Node dstNode = dstNodeC.getnodeconnectorNode();

        //log.info("Look up routing path");
        routing = (IRouting) ServiceHelper.getGlobalInstance(IRouting.class,
                this);
        mri = (MyRoutingImpl) routing;
        Path res;
        if (bwOrWeight == true){
            short bw = (short) (eermb*1000*8/eerms);
            mri.creatBwTopo(bw);
            res = mri.randomPath(srcNode, dstNode,bw,eermb);
        }else{
            res = mri.getWeightPath(srcNode, dstNode);
        }
        if (res == null) {
            if (!srcNode.equals(dstNode)){
                //log.info("Look up failure: no path avaiable");
                updatePerHostRuleInSW(srcNodeC, dstNodeC, srcPort, dstPort, srcNodeC.getnodeConnector().getNode(),
                        eerms,eermb,true);
                ArrayList dropList =  new ArrayList();
                dropList.add(bwOrWeight);
                dropList.add(srcNodeC);
                dropList.add(dstNodeC);
                dropList.add(dstPort);
                dropList.add(eermb);
                dropList.add(eerms);
                dropVector.add(dropList);
                log.info("the totel droped path is {}",dropVector.size());
                mri.dropVector = this.dropVector;
                return false;
            }
        }
        List<Edge> links = res.getEdges();
        if (links == null)
            log.info("Look up failure: no edge avaiable");

        LinkedList<FlowEntry> flowentries = new LinkedList<FlowEntry>();//flowentry缓存
        for (Edge link : links) {
            if (link == null) {
                log.error("Could not retrieve the Link");
                // TODO: should we keep going?
                continue;
            }
            flowentries.add(updatePerHostRuleInSW(srcNodeC, dstNodeC, srcPort, dstPort, link,eerms,eermb,true));
        }
        log.info("the installed eerpath is {}",res);
        FlowEntry firstFE = flowentries.getFirst();
        firstFEPathMap.put(firstFE,res);
        log.info("the firstFEPathMap size is {}",firstFEPathMap.size());
        mri.firstFEPathMap = this.firstFEPathMap;
        firstFEsMap.put(firstFE, flowentries);
        mri.firstFEsMap = this.firstFEsMap;
        if (bwOrWeight == true) {
            firstFEBwMap.put(firstFE,(short) (eermb*1000*8/eerms));
            mri.firstFEBwMap = this.firstFEBwMap;
            log.info("the eerflowentrybwmap size is {}",firstFEBwMap.size());
            mri.disOverLapFlowEntry = refreshEffectiveFlowEntry(mri.disOverLapFlowEntry,firstFE);
        }
        /*if (rs == null){u
            rs = new RandomSearch();
            new Thread(rs).start();
        }*/


        /*statisticsmanager = (IStatisticsManager) ServiceHelper.getGlobalInstance(
                IStatisticsManager.class, this);
        LinkedList<Path> paths= mri.weightusedpath;
        LinkedList<Path> finishedpaths = new LinkedList<Path>();
        while(true){
            for (Iterator<Path> it = paths.iterator(); it.hasNext();){
                Path path = it.next();
                Node startnode = path.getStartNode();
                List<FlowOnNode> flowsonnode = statisticsmanager.getFlows(startnode);
                for (Iterator<FlowOnNode> i = flowsonnode.iterator(); i.hasNext();){
                    FlowOnNode flowonnode = i.next();
                    Long packagenum = flowonnode.getPacketCount();
                    try{
                        Thread thread = Thread.currentThread();
                        thread.sleep(500);
                    }catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Long newpackagenum = flowonnode.getPacketCount();
                    if(packagenum == newpackagenum){
                        finishedpaths.add(path);
                        mri.recoverPath(path);
                    }
                }
            }
            paths.removeAll(finishedpaths);
        }*/

        /*try{
            Thread thread = Thread.currentThread();
            //暂停10秒后程序继续执行
            thread.sleep(5000);
        }catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Edge link : links) {
            if (link == null) {
                log.error("Could not retrieve the Link");
                // TODO: should we keep going?
                continue;
            }
            // Node currHeadNode=link.getHeadNodeConnector().getNode();
            // Node currTailNode=link.getTailNodeConnector().getNode();
            log.info("link tail {}/{} ->head {}/{}", link
                    .getTailNodeConnector().getNode(), link
                    .getTailNodeConnector(), link.getHeadNodeConnector()
                    .getNode(), link.getHeadNodeConnector());
            log.info(link.toString());
            deletePerHostRuleInSW(srcNodeC, dstNodeC, dstPort, srcPort, link,isAdd, eerms,eermb);
        }*/
        return true;
    }

/*    private boolean setWeightPathRoute(InetAddress srcAddr, InetAddress dstAddr,
            short dstPort, short srcPort, boolean isAdd, short eerms,short eermb) throws RuntimeException{
        hostTracker = (IfIptoHost) ServiceHelper.getGlobalInstance(
                IfIptoHost.class, this);
        log.info("srcNodeC = hostTracker. hostFind...hostTracker exists? ");
        if (hostTracker == null) {
            log.info("No, return!");
            return false;
        }
        log.info("Yes / {}:", hostTracker);
        Set<Node> nodes = null;
        // nodes = this.switchManager.getNodes();
        HostNodeConnector srcNodeC = hostTracker.hostFind(srcAddr);
        HostNodeConnector dstNodeC = hostTracker.hostFind(dstAddr);
        log.info("HostNode exists? {}", hostTracker == null);
        Node srcNode = srcNodeC.getnodeconnectorNode();
        Node dstNode = dstNodeC.getnodeconnectorNode();

        log.info("Look up routing path");
        routing = (IRouting) ServiceHelper.getGlobalInstance(IRouting.class,
                this);
        if (routing instanceof MyRoutingImpl){
            mri=(MyRoutingImpl) routing;
        }else{
            throw new RuntimeException("the routing model is not required");
        }
        if (mri != null)
            mri = null;
        mri = (MyRoutingImpl) routing;
        //mri.creatBwTopo(eermb);
        Path res = mri.getWeightPath(srcNode, dstNode);
        if (res == null) {
            log.info("Look up failure: no path avaiable");
            updatePerHostRuleInSW(srcNodeC, dstNodeC, dstPort, srcPort, srcNodeC.getnodeConnector().getNode(),
                    false, eerms,eermb,true);
            return false;
        }
        List<Edge> links = res.getEdges();
        if (links == null)
            log.info("Look up failure: no edge avaiable");

        for (Edge link : links) {
            if (link == null) {
                log.error("Could not retrieve the Link");
                // TODO: should we keep going?
                continue;
            }
            // Node currHeadNode=link.getHeadNodeConnector().getNode();
            // Node currTailNode=link.getTailNodeConnector().getNode();
            log.info("link tail {}/{} ->head {}/{}", link
                    .getTailNodeConnector().getNode(), link
                    .getTailNodeConnector(), link.getHeadNodeConnector()
                    .getNode(), link.getHeadNodeConnector());
            log.info(link.toString());
            updatePerHostRuleInSW(srcNodeC, dstNodeC, dstPort, srcPort, link.getTailNodeConnector().getNode(),
                    true, eerms,eermb,true);
        }
        pathflowmap.put(res,new LinkedList<FlowEntry>(flowentries));
        flowentries.clear();
        mri.pathflowmap = this.pathflowmap;
        if (rs == null){
            rs = new RandomSearch();
            new Thread(rs).start();
        }

        try{
            Thread thread = Thread.currentThread();
            //暂停10秒后程序继续执行
            thread.sleep(5000);
        }catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Edge link : links) {
            if (link == null) {
                log.error("Could not retrieve the Link");
                // TODO: should we keep going?
                continue;
            }
            // Node currHeadNode=link.getHeadNodeConnector().getNode();
            // Node currTailNode=link.getTailNodeConnector().getNode();
            log.info("link tail {}/{} ->head {}/{}", link
                    .getTailNodeConnector().getNode(), link
                    .getTailNodeConnector(), link.getHeadNodeConnector()
                    .getNode(), link.getHeadNodeConnector());
            log.info(link.toString());
            deletePerHostRuleInSW(srcNodeC, dstNodeC, dstPort, srcPort, link,isAdd, eerms,eermb);
        }
        return true;
    }*/

    private StartResult startSrcHost(InetAddress src, InetAddress dst,
            short srcPort, short dstPort, int eerms,int eermb ) throws IOException {
        //log.info("Assign Flow ReduceId {} ", eerms);
        boolean res = setPathRoute(src, dst, srcPort, dstPort,
                eerms,eermb,true);
        if (res == true) {
            log.info("Asign success!");
            return StartResult.Started;
        } else {
            log.info("Asign falure!");
            return StartResult.Notstarted;
        }
    }

    private EndResult endDstHost(InetAddress src, InetAddress dst,
            short srcPort, short dstPort, int eerms,int eermb) throws IOException {
        boolean res = setPathRoute(src, dst, srcPort, dstPort,
                eerms,eermb,false);
        if (res == true) {
            return EndResult.Ended;
        } else {
            return EndResult.Notended;
        }
    }

    @Override
    public Future<RpcResult<BEEROutput>> bEER(BEERInput destination) {
        try {
            InetAddress src = InetAddress.getByName(destination.getSrcAddr()
                    .getValue());
            InetAddress dst = InetAddress.getByName(destination.getDstAddr()
                    .getValue());
            short srcPort = destination.getSrcPort();
            short dstPort = destination.getDstPort();
            int eerms = destination.getMs();
            int eermb = destination.getMB();
            log.info("EER start Flow {}/{} to {}/{}", src, srcPort, dst,
                    dstPort);
            StartResult result = this.startSrcHost(src, dst, srcPort, dstPort,
                    eerms,eermb);

            /* Build the result and return it. */
            BEEROutputBuilder ob = new BEEROutputBuilder();
            ob.setStartResult(result);
            RpcResult<BEEROutput> rpcResult = Rpcs
                    .<BEEROutput> getRpcResult(true, ob.build(),
                            Collections.<RpcError> emptySet());

            return Futures.immediateFuture(rpcResult);
        } catch (Exception e) {

            /* Return error result. */
            BEEROutputBuilder ob = new BEEROutputBuilder();
            ob.setStartResult(StartResult.Error);
            RpcResult<BEEROutput> rpcResult = Rpcs
                    .<BEEROutput> getRpcResult(true, ob.build(),
                            Collections.<RpcError> emptySet());
            return Futures.immediateFuture(rpcResult);
        }
    }

    @Override
    public Future<RpcResult<EXROutput>> eXR(EXRInput destination) {
        try {
            InetAddress src = InetAddress.getByName(destination.getSrcAddr()
                    .getValue());
            InetAddress dst = InetAddress.getByName(destination.getDstAddr()
                    .getValue());
            short srcPort = destination.getSrcPort();
            short dstPort = destination.getDstPort();
            int eerms = destination.getMs();
            int eermb = destination.getMB();
            log.info("EER start Flow {} to {}", src, dst);
            EndResult result = this.endDstHost(src, dst, srcPort, dstPort,
                    eerms,eermb);
            /* Build the result and return it. */
            EXROutputBuilder ob = new EXROutputBuilder();
            ob.setEndResult(result);
            RpcResult<EXROutput> rpcResult = Rpcs
                    .<EXROutput> getRpcResult(true, ob.build(),
                            Collections.<RpcError> emptySet());

            return Futures.immediateFuture(rpcResult);
        } catch (Exception e) {

            /* Return error result. */
            EXROutputBuilder ob = new EXROutputBuilder();
            ob.setEndResult(EndResult.Error);
            RpcResult<EXROutput> rpcResult = Rpcs
                    .<EXROutput> getRpcResult(true, ob.build(),
                            Collections.<RpcError> emptySet());
            return Futures.immediateFuture(rpcResult);
        }
    }

    void init() {
        topologyManager = (ITopologyManager) ServiceHelper.getGlobalInstance(
                ITopologyManager.class, this);
        routing = (IRouting) ServiceHelper.getGlobalInstance(IRouting.class,
                this);
        hostTracker = (IfIptoHost) ServiceHelper.getGlobalInstance(
                IfIptoHost.class, this);
        switchManager = (ISwitchManager) ServiceHelper.getGlobalInstance(
                ISwitchManager.class, this);
        frm = (IForwardingRulesManager) ServiceHelper.getGlobalInstance(
                IForwardingRulesManager.class, this);
    }

    void destroy() {
    }

    void start() {
    }

    void stop() {
    }
}
