package org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput} instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput
 */
public class EXRInputBuilder {

    private Ipv4Address _dstAddr;
    private java.lang.Short _dstPort;
    private java.lang.Short _mB;
    private java.lang.Short _ms;
    private Ipv4Address _srcAddr;
    private java.lang.Short _srcPort;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> augmentation = new HashMap<>();

    public EXRInputBuilder() {
    } 

    public EXRInputBuilder(EXRInput base) {
        this._dstAddr = base.getDstAddr();
        this._dstPort = base.getDstPort();
        this._mB = base.getMB();
        this._ms = base.getMs();
        this._srcAddr = base.getSrcAddr();
        this._srcPort = base.getSrcPort();
        if (base instanceof EXRInputImpl) {
            EXRInputImpl _impl = (EXRInputImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public Ipv4Address getDstAddr() {
        return _dstAddr;
    }
    
    public java.lang.Short getDstPort() {
        return _dstPort;
    }
    
    public java.lang.Short getMB() {
        return _mB;
    }
    
    public java.lang.Short getMs() {
        return _ms;
    }
    
    public Ipv4Address getSrcAddr() {
        return _srcAddr;
    }
    
    public java.lang.Short getSrcPort() {
        return _srcPort;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public EXRInputBuilder setDstAddr(Ipv4Address value) {
        this._dstAddr = value;
        return this;
    }
    
    public EXRInputBuilder setDstPort(java.lang.Short value) {
        this._dstPort = value;
        return this;
    }
    
    public EXRInputBuilder setMB(java.lang.Short value) {
        this._mB = value;
        return this;
    }
    
    public EXRInputBuilder setMs(java.lang.Short value) {
        this._ms = value;
        return this;
    }
    
    public EXRInputBuilder setSrcAddr(Ipv4Address value) {
        this._srcAddr = value;
        return this;
    }
    
    public EXRInputBuilder setSrcPort(java.lang.Short value) {
        this._srcPort = value;
        return this;
    }
    
    public EXRInputBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public EXRInput build() {
        return new EXRInputImpl(this);
    }

    private static final class EXRInputImpl implements EXRInput {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput.class;
        }

        private final Ipv4Address _dstAddr;
        private final java.lang.Short _dstPort;
        private final java.lang.Short _mB;
        private final java.lang.Short _ms;
        private final Ipv4Address _srcAddr;
        private final java.lang.Short _srcPort;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> augmentation = new HashMap<>();

        private EXRInputImpl(EXRInputBuilder base) {
            this._dstAddr = base.getDstAddr();
            this._dstPort = base.getDstPort();
            this._mB = base.getMB();
            this._ms = base.getMs();
            this._srcAddr = base.getSrcAddr();
            this._srcPort = base.getSrcPort();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public Ipv4Address getDstAddr() {
            return _dstAddr;
        }
        
        @Override
        public java.lang.Short getDstPort() {
            return _dstPort;
        }
        
        @Override
        public java.lang.Short getMB() {
            return _mB;
        }
        
        @Override
        public java.lang.Short getMs() {
            return _ms;
        }
        
        @Override
        public Ipv4Address getSrcAddr() {
            return _srcAddr;
        }
        
        @Override
        public java.lang.Short getSrcPort() {
            return _srcPort;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_dstAddr == null) ? 0 : _dstAddr.hashCode());
            result = prime * result + ((_dstPort == null) ? 0 : _dstPort.hashCode());
            result = prime * result + ((_mB == null) ? 0 : _mB.hashCode());
            result = prime * result + ((_ms == null) ? 0 : _ms.hashCode());
            result = prime * result + ((_srcAddr == null) ? 0 : _srcAddr.hashCode());
            result = prime * result + ((_srcPort == null) ? 0 : _srcPort.hashCode());
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
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput other = (org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput)obj;
            if (_dstAddr == null) {
                if (other.getDstAddr() != null) {
                    return false;
                }
            } else if(!_dstAddr.equals(other.getDstAddr())) {
                return false;
            }
            if (_dstPort == null) {
                if (other.getDstPort() != null) {
                    return false;
                }
            } else if(!_dstPort.equals(other.getDstPort())) {
                return false;
            }
            if (_mB == null) {
                if (other.getMB() != null) {
                    return false;
                }
            } else if(!_mB.equals(other.getMB())) {
                return false;
            }
            if (_ms == null) {
                if (other.getMs() != null) {
                    return false;
                }
            } else if(!_ms.equals(other.getMs())) {
                return false;
            }
            if (_srcAddr == null) {
                if (other.getSrcAddr() != null) {
                    return false;
                }
            } else if(!_srcAddr.equals(other.getSrcAddr())) {
                return false;
            }
            if (_srcPort == null) {
                if (other.getSrcPort() != null) {
                    return false;
                }
            } else if(!_srcPort.equals(other.getSrcPort())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                EXRInputImpl otherImpl = (EXRInputImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EXRInput>> e : augmentation.entrySet()) {
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
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("EXRInput [");
            boolean first = true;
        
            if (_dstAddr != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_dstAddr=");
                builder.append(_dstAddr);
             }
            if (_dstPort != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_dstPort=");
                builder.append(_dstPort);
             }
            if (_mB != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_mB=");
                builder.append(_mB);
             }
            if (_ms != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_ms=");
                builder.append(_ms);
             }
            if (_srcAddr != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_srcAddr=");
                builder.append(_srcAddr);
             }
            if (_srcPort != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_srcPort=");
                builder.append(_srcPort);
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
