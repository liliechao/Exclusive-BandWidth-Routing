package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>EER</b>
 * <br />(Source path: <i>META-INF\yang\EER.yang</i>):
 * <pre>
 * container output {
 *     leaf end-result {
 *         type enumeration;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>EER/EXR/output</i>
 * 
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutputBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutputBuilder
 */
public interface EXROutput
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>
{


    /**
     * The enumeration built-in type represents values from a set of assigned names.
     */
    public enum EndResult {
        /**
         * ended
         */
        Ended(0),
        
        /**
         * not ended
         */
        Notended(1),
        
        /**
         * Error happened
         */
        Error(2)
        ;
    
    
        int value;
        static java.util.Map<java.lang.Integer, EndResult> valueMap;
    
        static {
            valueMap = new java.util.HashMap<>();
            for (EndResult enumItem : EndResult.values())
            {
                valueMap.put(enumItem.value, enumItem);
            }
        }
    
        private EndResult(int value) {
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
         * @return corresponding EndResult item
         */
        public static EndResult forValue(int valueArg) {
            return valueMap.get(valueArg);
        }
    }

    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:EER","2013-09-11","output");;

    /**
     * Result types
     */
    EndResult getEndResult();

}

