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
package org.apache.nifi.registry.flow;

import org.apache.nifi.components.AbstractConfigurableComponent;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.Optional;
import javax.net.ssl.SSLContext;

public abstract class AbstractFlowRegistryClient extends AbstractConfigurableComponent implements FlowRegistryClient {

    /**
     * Property that controls how often NiFi checks this Flow Registry Client for updated versions of the flows that
     * are under version control against it. When this property is left blank, NiFi falls back to the global
     * {@code nifi.flowcontroller.registry.sync.interval} property. Concrete Flow Registry Client implementations should
     * include this descriptor in the List returned by {@link #getSupportedPropertyDescriptors()} in order to allow the
     * synchronization interval to be configured on a per-client basis.
     */
    public static final PropertyDescriptor SYNCHRONIZATION_INTERVAL = new PropertyDescriptor.Builder()
            .name("Synchronization Interval")
            .description("""
                    How often NiFi checks this Flow Registry Client for newer versions of the flows that are under version control against it. \
                    When left blank, the application uses the nifi.flowcontroller.registry.sync.interval property""")
            .required(false)
            .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
            .build();

    private volatile String identifier;
    private volatile Optional<SSLContext> systemSslContext;
    private volatile ComponentLog logger;

    @Override
    public void initialize(final FlowRegistryClientInitializationContext context) {
        this.identifier = context.getIdentifier();
        this.logger = context.getLogger();
        this.systemSslContext = context.getSystemSslContext();
    }

    @Override
    public final String getIdentifier() {
        return identifier;
    }

    protected final ComponentLog getLogger() {
        return logger;
    }

    protected final Optional<SSLContext> getSystemSslContext() {
        return systemSslContext;
    }
}
