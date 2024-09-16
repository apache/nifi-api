/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.controller;

import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.AbstractConfigurableComponent;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.reporting.InitializationException;

public abstract class AbstractControllerService extends AbstractConfigurableComponent implements ControllerService {

    private String identifier;
    private ControllerServiceLookup serviceLookup;
    private ComponentLog logger;
    private StateManager stateManager;
    private volatile ConfigurationContext configurationContext;
    private volatile boolean enabled = false;
    private NodeTypeProvider nodeTypeProvider;

    @Override
    public final void initialize(final ControllerServiceInitializationContext context) throws InitializationException {
        this.identifier = context.getIdentifier();
        serviceLookup = context.getControllerServiceLookup();
        logger = context.getLogger();
        stateManager = context.getStateManager();
        nodeTypeProvider = context.getNodeTypeProvider();
        init(context);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the {@link ControllerServiceLookup} that was passed to the
     * {@link #init(ControllerServiceInitializationContext)} method
     */
    protected final ControllerServiceLookup getControllerServiceLookup() {
        return serviceLookup;
    }

    /**
     * @return the {@link NodeTypeProvider} that was passed to the
     * {@link #init(ControllerServiceInitializationContext)} method
     */
    protected final NodeTypeProvider getNodeTypeProvider() {
        return nodeTypeProvider;
    }

    /**
     * Provides a mechanism by which subclasses can perform initialization of
     * the Controller Service before it is scheduled to be run
     *
     * @param config of initialization context
     * @throws InitializationException if unable to init
     */
    protected void init(final ControllerServiceInitializationContext config) throws InitializationException {
    }

    @OnEnabled
    public final void enabled() {
        this.enabled = true;
    }

    @OnDisabled
    public final void disabled() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @return the logger that has been provided to the component by the
     * framework in its initialize method
     */
    protected ComponentLog getLogger() {
        return logger;
    }

    /**
     * @return the StateManager that can be used to store and retrieve state for this Controller Service
     */
    protected StateManager getStateManager() {
        return stateManager;
    }

    @OnEnabled
    public final void abstractStoreConfigContext(final ConfigurationContext configContext) {
        this.configurationContext = configContext;
    }

    @OnDisabled
    public final void abstractClearConfigContext() {
        this.configurationContext = null;
    }

    protected ConfigurationContext getConfigurationContext() {
        final ConfigurationContext context = this.configurationContext;
        if (context == null) {
            throw new IllegalStateException("No Configuration Context exists");
        }

        return configurationContext;
    }

    protected PropertyValue getProperty(final PropertyDescriptor descriptor) {
        return getConfigurationContext().getProperty(descriptor);
    }
}
