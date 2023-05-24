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
package org.apache.camel.component.jslt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import org.apache.camel.Category;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.ObjectHelper;

/**
 * Query or transform JSON payloads using an JSLT.
 */
@UriEndpoint(firstVersion = "3.1.0", scheme = "jslt", title = "JSLT", syntax = "jslt:resourceUri", producerOnly = true,
             category = { Category.TRANSFORMATION }, headersClass = JsltConstants.class)
public class JsltEndpoint extends ResourceEndpoint {

    private Expression transform;

    @UriParam(defaultValue = "false")
    private boolean allowTemplateFromHeader;
    @UriParam(defaultValue = "false", label = "common")
    private boolean prettyPrint;
    @UriParam(defaultValue = "false")
    private boolean mapBigDecimalAsFloats;
    @UriParam
    private ObjectMapper objectMapper;

    public JsltEndpoint() {
    }

    public JsltEndpoint(String uri, JsltComponent component, String resourceUri) {
        super(uri, component, resourceUri);
    }

    @Override
    public ExchangePattern getExchangePattern() {
        return ExchangePattern.InOut;
    }

    @Override
    protected String createEndpointUri() {
        return "jslt:" + getResourceUri();
    }

    public JsltEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
        String newUri = uri.replace(getResourceUri(), newResourceUri);
        log.debug("Getting endpoint with URI: {}", newUri);
        return getCamelContext().getEndpoint(newUri, JsltEndpoint.class);
    }

    @Override
    protected void onExchange(Exchange exchange) throws Exception {
        String path = getResourceUri();
        ObjectHelper.notNull(path, "resourceUri");

        String newResourceUri = null;
        if (allowTemplateFromHeader) {
            newResourceUri = exchange.getIn().getHeader(JsltConstants.HEADER_JSLT_RESOURCE_URI, String.class);
        }
        if (newResourceUri != null) {
            exchange.getIn().removeHeader(JsltConstants.HEADER_JSLT_RESOURCE_URI);

            log.debug("{} set to {} creating new endpoint to handle exchange", JsltConstants.HEADER_JSLT_RESOURCE_URI,
                    newResourceUri);
            JsltEndpoint newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri);
            newEndpoint.onExchange(exchange);
            return;
        }

        getProcessor().process(exchange);

    }

    /**
     * If true, JSON in output message is pretty printed.
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isAllowTemplateFromHeader() {
        return allowTemplateFromHeader;
    }

    /**
     * Whether to allow to use resource template from header or not (default false).
     *
     * Enabling this allows to specify dynamic templates via message header. However this can be seen as a potential
     * security vulnerability if the header is coming from a malicious user, so use this with care.
     */
    public void setAllowTemplateFromHeader(boolean allowTemplateFromHeader) {
        this.allowTemplateFromHeader = allowTemplateFromHeader;
    }

    public boolean isMapBigDecimalAsFloats() {
        return mapBigDecimalAsFloats;
    }

    /**
     * If true, the mapper will use the USE_BIG_DECIMAL_FOR_FLOATS in serialization features
     */
    public void setMapBigDecimalAsFloats(boolean mapBigDecimalAsFloats) {
        this.mapBigDecimalAsFloats = mapBigDecimalAsFloats;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Setting a custom JSON Object Mapper to be used
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Processor createProcessor() throws Exception {
        final JsltProcessor processor = new JsltProcessor();
        final JsltComponent component = (JsltComponent) getComponent();
        processor.setAllowContextMapAll(isAllowContextMapAll());
        processor.setAllowTemplateFromHeader(isAllowTemplateFromHeader());
        processor.setPrettyPrint(isPrettyPrint());
        processor.setMapBigDecimalAsFloats(isMapBigDecimalAsFloats());
        processor.setObjectMapper(getObjectMapper());
        processor.setResourceUri(getResourceUri());

        processor.setFunctions(component.getFunctions());
        processor.setObjectFilter(component.getObjectFilter());

        processor.setCamelContext(getCamelContext());
        return processor;
    }
}
