package com.dtstack.taier.datasource.plugin.common.service;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.session.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractService implements Service {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);
    protected TaierConf conf;
    protected ServiceState state;
    protected long startTime;
    protected final String serviceName;

    public AbstractService(String serviceName) {
        this.serviceName = serviceName;
        this.state = ServiceState.LATENT;
    }

    @Override
    public void initialize(TaierConf conf) {
        ensureCurrentState(ServiceState.LATENT);
        this.conf = conf;
        changeState(ServiceState.INITIALIZED);
        LOGGER.info("Service[" + serviceName + "] is initialized.");
    }

    @Override
    public void start() {
        ensureCurrentState(ServiceState.INITIALIZED);
        this.startTime = System.currentTimeMillis();
        changeState(ServiceState.STARTED);
        LOGGER.info("Service[" + serviceName + "] is started.");
    }

    @Override
    public void stop() {
        switch (state) {
            case LATENT:
            case INITIALIZED:
            case STOPPED:
                LOGGER.warn("Service[" + serviceName + "] is not started(" + state + ") yet.");
                break;
            default:
                ensureCurrentState(ServiceState.STARTED);
                changeState(ServiceState.STOPPED);
                LOGGER.info("Service[" + serviceName + "] is stopped.");
        }
    }

    @Override
    public String getName() {
        return serviceName;
    }

//    @Override
//    public TaierConf getConf() {
//        return conf;
//    }

    @Override
    public ServiceState getServiceState() {
        return state;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    private void ensureCurrentState(ServiceState currentState) {
        if (state != currentState) {
            throw new IllegalStateException(
                    "For this operation, the current service state must be " + currentState +
                            " instead of " + state);
        }
    }

    private void changeState(ServiceState newState) {
        state = newState;
    }

    public TaierConf getConf() {
        return conf;
    }
}
