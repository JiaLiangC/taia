package com.dtstack.taier.datasource.plugin.common.service;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.session.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CompositeService extends AbstractService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CompositeService.class);
    private final List<Service> serviceList = new ArrayList<>();

    public CompositeService(String serviceName) {
        super(serviceName);
    }

    public List<Service> getServices() {
        return Collections.unmodifiableList(serviceList);
    }

    protected void addService(Service service) {
        serviceList.add(service);
    }

    @Override
    public void initialize(TaierConf conf) {
        for (Service service : serviceList) {
            service.initialize(conf);
        }
        super.initialize(conf);
    }

 /*   @Override
    public void initialize(TaierConf conf) {
        for (Service service : serviceList) {
            service.initialize(conf);
        }
        super.initialize(conf);
    }*/

    @Override
    public void start() {
        for (int i = 0; i < serviceList.size(); i++) {
            Service service = serviceList.get(i);
            try {
                service.start();
            } catch (Exception e) {
                LOGGER.error("Error starting service " + service.getName(), e);
                stop(i);
//                throw new TaierException("Failed to Start " + getName(), e);
            }
        }
        super.start();
    }

    @Override
    public void stop() {
        if (getServiceState() == ServiceState.STOPPED) {
            LOGGER.warn("Service[" + getName() + "] is stopped already");
        } else {
            stop(getServices().size());
        }
        super.stop();
    }

    /**
     * stop in reverse order of start
     * @param numOfServicesStarted num of services which are in start state
     */
    private void stop(int numOfServicesStarted) {
        for (int i = numOfServicesStarted - 1; i >= 0; i--) {
            Service service = serviceList.get(i);
            try {
                LOGGER.info("Service: [" + service.getName() + "] is stopping.");
                service.stop();
            } catch (Throwable t) {
                LOGGER.warn("Error stopping " + service.getName());
            }
        }
    }
}

