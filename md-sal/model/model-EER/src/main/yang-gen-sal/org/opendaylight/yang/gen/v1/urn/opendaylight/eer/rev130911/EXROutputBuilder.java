package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import java.util.Collections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput.EndResult;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput
 */
public class EXROutputBuilder {

    private EndResult _endResult;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> augmentation = new HashMap<>();

    public EXROutputBuilder() {
    } 

    public EXROutputBuilder(EXROutput base) {
        this._endResult = base.getEndResult();
        if (base instanceof EXROutputImpl) {
            EXROutputImpl _impl = (EXROutputImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public EndResult getEndResult() {
        return _endResult;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public EXROutputBuilder setEndResult(EndResult value) {
        this._endResult = value;
        return this;
    }
    
    public EXROutputBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public EXROutput build() {
        return new EXROutputImpl(this);
    }

    private static final class EXROutputImpl implements EXROutput {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput.class;
        }

        private final EndResult _endResult;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> augmentation = new HashMap<>();

        private EXROutputImpl(EXROutputBuilder base) {
            this._endResult = base.getEndResult();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public EndResult getEndResult() {
            return _endResult;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_endResult == null) ? 0 : _endResult.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput other = (org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput)obj;
            if (_endResult == null) {
                if (other.getEndResult() != null) {
                    return false;
                }
            } else if(!_endResult.equals(other.getEndResult())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                EXROutputImpl otherImpl = (EXROutputImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXROutput>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("EXROutput [");
            boolean first = true;
        
            if (_endResult != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_endResult=");
                builder.append(_endResult);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
