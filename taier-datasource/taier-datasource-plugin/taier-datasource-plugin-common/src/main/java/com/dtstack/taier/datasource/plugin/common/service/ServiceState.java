package com.dtstack.taier.datasource.plugin.common.service;

public enum ServiceState {
    /**
     * Constructed but not initialized
     */
    LATENT,

    /**
     * Initialized but not started or stopped
     */
    INITIALIZED,

    /**
     * Started and not stopped
     */
    STARTED,

    /**
     * Stopped. No further state transitions are permitted
     */
    STOPPED
}