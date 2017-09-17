package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>EER</b>
 * <br />(Source path: <i>META-INF\yang\EER.yang</i>):
 * <pre>
 * container output {
 *     leaf start-result {
 *         type enumeration;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>EER/BEER/output</i>
 * 
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutputBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutputBuilder
 */
public interface BEEROutput
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>
{


    /**
     * The enumeration built-in type represents values from a set of assigned names.
     */
    public enum StartResult {
        /**
         * started
         */
        Started(0),
        
        /**
         * not started
         */
        Notstarted(1),
        
        /**
         * Error happened
         */
        Error(2)
        ;
    
    
        int value;
        static java.util.Map<java.lang.Integer, StartResult> valueMap;
    
        static {
            valueMap = new java.util.HashMap<>();
            for (StartResult enumItem : StartResult.values())
            {
                valueMap.put(enumItem.value, enumItem);
            }
        }
    
        private StartResult(int value) {
            this.value = value;
        }
        
        /**
         * @return integer value
         */
        public int getIntValue() {
            return value;
        }
    
        /**
         * @param valueArg
         * @return corresponding StartResult item
         */
        public static StartResult forValue(int valueArg) {
            return valueMap.get(valueArg);
        }
    }

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:EER","2013-09-11","output");;

    /**
     * Result types
     */
    StartResult getStartResult();

}

