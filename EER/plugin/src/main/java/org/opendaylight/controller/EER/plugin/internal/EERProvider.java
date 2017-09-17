package org.opendaylight.controller.EER.plugin.internal;

import java.util.Collection;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eer.rev130911.EERService;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EERProvider extends AbstractBindingAwareProvider {
    // A BindingAwareProvider interface requires the implementation of four
    // methods, and registering an instance with BindingAwareBroker. Use
    // AbstractBindingAwareProvider to simplify the implementation.

    // private DependencyManager mydm;

    private final static Logger LOG = LoggerFactory
            .getLogger(EERProvider.class);
    // private DataProviderService dataProviderService;
    // private DataBrokerService dataService;
    EERImpl eerImpl;
    private ProviderContext session;
    private RpcRegistration<EERService> registration;
    private ITopologyManager topologyManager;

    public void setTopologyManager(ITopologyManager topologyManager) {
        LOG.debug("Setting topologyManager Mapreduce");
        this.topologyManager = topologyManager;
    }

    public void unsetTopologyManager(ITopologyManager topologyManager) {
        if (this.topologyManager == topologyManager) {
            this.topologyManager = null;
        }
    }

    public EERProvider() {

    }

    @Override
    public Collection<? extends RpcService> getImplementations() {
        // Shorthand registration of an already instantiated implementations of
        // global RPC services.
        // Automated registration is currently not supported.
        return null;
    }

    @Override
    public Collection<? extends ProviderFunctionality> getFunctionality() {
        // Shorthand registration of an already instantiated implementations of
        // ProviderFunctionality.
        return null;
    }


    @Override
    public void onSessionInitiated(ProviderContext session) {

        eerImpl = new EERImpl();

        LOG.debug("Test Plugin Started");

        try {
            this.registration = session.addRpcImplementation(
                    EERService.class, eerImpl);
            // context.registerService( IContainerAware.class.getName(), this,
            // null);
            // session.registerFunctionality(ITopologyManager.class.getName());

        } catch (Exception e) {
            LOG.debug("Error in addRpcImplementation", e);
        }

    }

    @Override
    protected void startImpl(BundleContext context) {

        // this.mydm = new DependencyManager(context);

    }

}