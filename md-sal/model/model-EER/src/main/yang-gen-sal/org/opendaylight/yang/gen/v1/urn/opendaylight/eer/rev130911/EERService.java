package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEERInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput;


/**
 * Interface for implementing the following YANG RPCs defined in module <b>EER</b>
 * <br />(Source path: <i>META-INF\yang\EER.yang</i>):
 * <pre>
 * rpc BEER {
 *     "Starting a mapreduce flow";
 *     input {
 *         leaf srcAddr {
 *             type ipv4-address;
 *         }
 *         leaf srcPort {
 *             type int16;
 *         }
 *         leaf dstAddr {
 *             type ipv4-address;
 *         }
 *         leaf dstPort {
 *             type int16;
 *         }
 *         leaf ms {
 *             type int32;
 *         }
 *         leaf MB {
 *             type int32;
 *         }
 *     }
 *     
 *     output {
 *         leaf start-result {
 *             type enumeration;
 *         }
 *     }
 *     status CURRENT;
 * }
 * rpc EXR {
 *     "Starting a mapreduce flow";
 *     input {
 *         leaf srcAddr {
 *             type ipv4-address;
 *         }
 *         leaf srcPort {
 *             type int16;
 *         }
 *         leaf dstAddr {
 *             type ipv4-address;
 *         }
 *         leaf dstPort {
 *             type int16;
 *         }
 *         leaf ms {
 *             type int16;
 *         }
 *         leaf MB {
 *             type int16;
 *         }
 *     }
 *     
 *     output {
 *         leaf end-result {
 *             type enumeration;
 *         }
 *     }
 *     status CURRENT;
 * }
 * </pre>
 */
public interface EERService
    extends
    RpcService
{




    /**
     * Starting a mapreduce flow
     */
    Future<RpcResult<BEEROutput>> bEER(BEERInput input);
    
    /**
     * Starting a mapreduce flow
     */
    Future<RpcResult<EXROutput>> eXR(EXRInput input);

}

