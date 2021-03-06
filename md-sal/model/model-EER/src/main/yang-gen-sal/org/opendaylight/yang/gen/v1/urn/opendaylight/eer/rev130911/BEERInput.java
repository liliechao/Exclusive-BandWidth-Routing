package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>EER</b>
 * <br />(Source path: <i>META-INF\yang\EER.yang</i>):
 * <pre>
 * container input {
 *     leaf srcAddr {
 *         type ipv4-address;
 *     }
 *     leaf srcPort {
 *         type int16;
 *     }
 *     leaf dstAddr {
 *         type ipv4-address;
 *     }
 *     leaf dstPort {
 *         type int16;
 *     }
 *     leaf ms {
 *         type int32;
 *     }
 *     leaf MB {
 *         type int32;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>EER/BEER/input</i>
 * 
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEERInputBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEERInputBuilder
 */
public interface BEERInput
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEERInput>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:EER","2013-09-11","input");;

    Ipv4Address getSrcAddr();
    
    java.lang.Short getSrcPort();
    
    Ipv4Address getDstAddr();
    
    java.lang.Short getDstPort();
    
    java.lang.Integer getMs();
    
    java.lang.Integer getMB();

}

