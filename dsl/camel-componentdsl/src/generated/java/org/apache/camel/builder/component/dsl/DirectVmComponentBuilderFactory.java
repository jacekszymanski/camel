/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.builder.component.dsl;

import javax.annotation.Generated;
import org.apache.camel.Component;
import org.apache.camel.builder.component.AbstractComponentBuilder;
import org.apache.camel.builder.component.ComponentBuilder;
import org.apache.camel.component.directvm.DirectVmComponent;

/**
 * Call another endpoint from any Camel Context in the same JVM synchronously.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface DirectVmComponentBuilderFactory {

    /**
     * Direct VM (camel-directvm)
     * Call another endpoint from any Camel Context in the same JVM
     * synchronously.
     * 
     * Category: core,endpoint
     * Since: 2.10
     * Maven coordinates: org.apache.camel:camel-directvm
     * 
     * @return the dsl builder
     */
    @Deprecated
    static DirectVmComponentBuilder directVm() {
        return new DirectVmComponentBuilderImpl();
    }

    /**
     * Builder for the Direct VM component.
     */
    interface DirectVmComponentBuilder
            extends
                ComponentBuilder<DirectVmComponent> {
        /**
         * Allows for bridging the consumer to the Camel routing Error Handler,
         * which mean any exceptions occurred while the consumer is trying to
         * pickup incoming messages, or the likes, will now be processed as a
         * message and handled by the routing Error Handler. By default the
         * consumer will use the org.apache.camel.spi.ExceptionHandler to deal
         * with exceptions, that will be logged at WARN or ERROR level and
         * ignored.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: consumer
         * 
         * @param bridgeErrorHandler the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder bridgeErrorHandler(
                boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
            return this;
        }
        /**
         * If sending a message to a direct endpoint which has no active
         * consumer, then we can tell the producer to block and wait for the
         * consumer to become active.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param block the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder block(boolean block) {
            doSetProperty("block", block);
            return this;
        }
        /**
         * Whether the producer should be started lazy (on the first message).
         * By starting lazy you can use this to allow CamelContext and routes to
         * startup in situations where a producer may otherwise fail during
         * starting and cause the route to fail being started. By deferring this
         * startup to be lazy then the startup failure can be handled during
         * routing messages via Camel's routing error handlers. Beware that when
         * the first message is processed then creating and starting the
         * producer may take a little time and prolong the total processing time
         * of the processing.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder lazyStartProducer(
                boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
        /**
         * The timeout value to use if block is enabled.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Default: 30000
         * Group: producer
         * 
         * @param timeout the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder timeout(long timeout) {
            doSetProperty("timeout", timeout);
            return this;
        }
        /**
         * Whether autowiring is enabled. This is used for automatic autowiring
         * options (the option must be marked as autowired) by looking up in the
         * registry to find if there is a single instance of matching type,
         * which then gets configured on the component. This can be used for
         * automatic configuring JDBC data sources, JMS connection factories,
         * AWS Clients, etc.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param autowiredEnabled the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder autowiredEnabled(
                boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
        /**
         * Sets a HeaderFilterStrategy that will only be applied on producer
         * endpoints (on both directions: request and response). Default value:
         * none.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.spi.HeaderFilterStrategy&lt;/code&gt;
         * type.
         * 
         * Group: advanced
         * 
         * @param headerFilterStrategy the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder headerFilterStrategy(
                org.apache.camel.spi.HeaderFilterStrategy headerFilterStrategy) {
            doSetProperty("headerFilterStrategy", headerFilterStrategy);
            return this;
        }
        /**
         * Whether to propagate or not properties from the producer side to the
         * consumer side, and vice versa. Default value: true.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param propagateProperties the value to set
         * @return the dsl builder
         */
        default DirectVmComponentBuilder propagateProperties(
                boolean propagateProperties) {
            doSetProperty("propagateProperties", propagateProperties);
            return this;
        }
    }

    class DirectVmComponentBuilderImpl
            extends
                AbstractComponentBuilder<DirectVmComponent>
            implements
                DirectVmComponentBuilder {
        @Override
        protected DirectVmComponent buildConcreteComponent() {
            return new DirectVmComponent();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "bridgeErrorHandler": ((DirectVmComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "block": ((DirectVmComponent) component).setBlock((boolean) value); return true;
            case "lazyStartProducer": ((DirectVmComponent) component).setLazyStartProducer((boolean) value); return true;
            case "timeout": ((DirectVmComponent) component).setTimeout((long) value); return true;
            case "autowiredEnabled": ((DirectVmComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "headerFilterStrategy": ((DirectVmComponent) component).setHeaderFilterStrategy((org.apache.camel.spi.HeaderFilterStrategy) value); return true;
            case "propagateProperties": ((DirectVmComponent) component).setPropagateProperties((boolean) value); return true;
            default: return false;
            }
        }
    }
}