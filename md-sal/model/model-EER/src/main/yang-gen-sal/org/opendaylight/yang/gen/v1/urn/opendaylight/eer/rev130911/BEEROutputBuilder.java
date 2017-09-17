package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput.StartResult;
import java.util.Collections;
import java.util.Map;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput
 */
public class BEEROutputBuilder {

    private StartResult _startResult;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> augmentation = new HashMap<>();

    public BEEROutputBuilder() {
    } 

    public BEEROutputBuilder(BEEROutput base) {
        this._startResult = base.getStartResult();
        if (base instanceof BEEROutputImpl) {
            BEEROutputImpl _impl = (BEEROutputImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public StartResult getStartResult() {
        return _startResult;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public BEEROutputBuilder setStartResult(StartResult value) {
        this._startResult = value;
        return this;
    }
    
    public BEEROutputBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public BEEROutput build() {
        return new BEEROutputImpl(this);
    }

    private static final class BEEROutputImpl implements BEEROutput {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput.class;
        }

        private final StartResult _startResult;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> augmentation = new HashMap<>();

        private BEEROutputImpl(BEEROutputBuilder base) {
            this._startResult = base.getStartResult();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public StartResult getStartResult() {
            return _startResult;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_startResult == null) ? 0 : _startResult.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput other = (org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput)obj;
            if (_startResult == null) {
                if (other.getStartResult() != null) {
                    return false;
                }
            } else if(!_startResult.equals(other.getStartResult())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                BEEROutputImpl otherImpl = (BEEROutputImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.BEEROutput>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("BEEROutput [");
            boolean first = true;
        
            if (_startResult != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_startResult=");
                builder.append(_startResult);
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
