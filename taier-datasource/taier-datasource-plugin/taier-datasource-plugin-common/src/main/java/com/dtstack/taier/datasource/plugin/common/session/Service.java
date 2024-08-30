package com.dtstack.taier.datasource.plugin.common.session;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.service.ServiceState;

public interface Service {

    /**
     * Initialize the service.
     *
     * The transition must be from LATENT to INITIALIZED unless the
     * operation failed and an exception was raised.
     *
     */
//    void initialize(TaierConf conf);

    void initialize(TaierConf conf);
    /**
     * Start the service.
     *
     * The transition should be from INITIALIZED to STARTED unless the
     * operation failed and an exception was raised.
     */
    void start();

    /**
     * Stop the service.
     *
     * This operation must be designed to complete regardless of the initial state
     * of the service, including the state of all its internal fields.
     */
    void stop();

    /**
     * Get the name of this service.
     *
     * @return the service name
     */
    String getName();

    /**
     * Get the configuration of this service.
     * This is normally not a clone and may be manipulated, though there are no
     * guarantees as to what the consequences of such actions may be
     *
     * @return the current configuration, unless a specific implementation chooses
     *         otherwise.
     */
//    TaierConf getConf();

    /**
     * Get the current service state
     *
     * @return the state of the service
     */
    ServiceState getServiceState();

    /**
     * Get the service start time
     *
     * @return the start time of the service. This will be zero if the service
     *         has not yet been started.
     */
    long getStartTime();
}
