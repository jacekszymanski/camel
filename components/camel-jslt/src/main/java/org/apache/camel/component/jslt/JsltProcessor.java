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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.filters.DefaultJsonFilter;
import com.schibsted.spt.data.jslt.filters.JsonFilter;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.ValidationException;
import org.apache.camel.WrappedFile;
import org.apache.camel.support.ExchangeHelper;
import org.apache.camel.support.ResourceHelper;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsltProcessor implements Processor, org.apache.camel.Expression, Predicate {
    private static final String URI_PREFIX = "jslt:";

    private static final ObjectMapper OBJECT_MAPPER;
    private static final JsonFilter DEFAULT_JSON_FILTER = new DefaultJsonFilter();

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializerFactory(OBJECT_MAPPER.getSerializerFactory().withSerializerModifier(
                new SafeTypesOnlySerializerModifier()));
    }

    private static final Logger LOG = LoggerFactory.getLogger(JsltProcessor.class);

    private boolean allowContextMapAll;
    private boolean allowTemplateFromHeader;
    private boolean prettyPrint;
    private boolean mapBigDecimalAsFloats;
    private ObjectMapper objectMapper;
    private String resourceUri;
    private CamelContext camelContext;
    private String expressionText; // only setter, used only for Expression & Predicate

    private Collection<Function> functions;
    private JsonFilter objectFilter;

    private Expression transform;

    @Override
    public void process(Exchange exchange) throws Exception {
        String result = applyExpression(exchange);
        ExchangeHelper.setInOutBodyPatternAware(exchange, result);
    }

    private String applyExpression(Exchange exchange) throws Exception, ValidationException, IOException {
        JsonNode input = getInputFromExchange(exchange);
        Map<String, JsonNode> variables = extractVariables(exchange);
        JsonNode output = getTransform(exchange.getMessage()).apply(variables, input);
        String result = isPrettyPrint() ? output.toPrettyString() : output.toString();
        return result;
    }

    private synchronized Expression getTransform(Message msg) throws Exception {
        final String jsltStringFromHeader
                = allowTemplateFromHeader ? msg.getHeader(JsltConstants.HEADER_JSLT_STRING, String.class) : null;

        final boolean useTemplateFromUri = jsltStringFromHeader == null;

        if (useTemplateFromUri && transform != null) {
            return transform;
        }

        final String transformSource;
        final InputStream stream;

        if (useTemplateFromUri) {
            transformSource = getResourceUri();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Jslt content read from resource {} with resourceUri: {} for endpoint {}",
                        transformSource,
                        transformSource,
                        getEndpointUri());
            }

            stream = ResourceHelper.resolveMandatoryResourceAsInputStream(getCamelContext(), transformSource);
            if (stream == null) {
                throw new JsltException("Cannot load resource '" + transformSource + "': not found");
            }
        } else { // use template from header
            stream = new ByteArrayInputStream(jsltStringFromHeader.getBytes(StandardCharsets.UTF_8));
            transformSource = "<inline>";
        }

        final Expression transform;
        try {
            transform = createExpression(stream, transformSource);
        } finally {
            // the stream is consumed only on .compile(), cannot be closed before
            IOHelper.close(stream);
        }

        if (useTemplateFromUri) {
            this.transform = transform;
        }
        return transform;
    }

    /**
     * Extract the variables from the headers in the message.
     */
    private Map<String, JsonNode> extractVariables(Exchange exchange) {
        Map<String, Object> variableMap = ExchangeHelper.createVariableMap(exchange, isAllowContextMapAll());
        Map<String, JsonNode> serializedVariableMap = new HashMap<>();
        if (variableMap.containsKey("headers")) {
            serializedVariableMap.put("headers", serializeMapToJsonNode((Map<String, Object>) variableMap.get("headers")));
        }
        if (variableMap.containsKey("exchange")) {
            Exchange ex = (Exchange) variableMap.get("exchange");
            ObjectNode exchangeNode = OBJECT_MAPPER.createObjectNode();
            if (ex.getProperties() != null) {
                exchangeNode.set("properties", serializeMapToJsonNode(ex.getProperties()));
            }
            serializedVariableMap.put("exchange", exchangeNode);
        }
        return serializedVariableMap;
    }

    private ObjectNode serializeMapToJsonNode(Map<String, Object> map) {
        ObjectNode mapNode = OBJECT_MAPPER.createObjectNode();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    // Use Jackson to convert value to JsonNode
                    mapNode.set(entry.getKey(), OBJECT_MAPPER.valueToTree(entry.getValue()));
                } catch (IllegalArgumentException e) {
                    //If Jackson cannot convert the value to json (e.g. infinite recursion in the value to serialize)
                    LOG.debug("Value could not be converted to JsonNode", e);
                }
            }
        }
        return mapNode;
    }

    private JsonNode getInputFromExchange(Exchange exchange) throws IOException, ValidationException {
        final JsonNode input;
        final ObjectMapper objectMapper;

        if (ObjectHelper.isEmpty(getObjectMapper())) {
            objectMapper = new ObjectMapper();
        } else {
            objectMapper = getObjectMapper();
        }
        if (isMapBigDecimalAsFloats()) {
            objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        }

        Object body = exchange.getIn().getBody();
        if (body instanceof WrappedFile) {
            body = ((WrappedFile<?>) body).getFile();
        }
        if (body instanceof String) {
            input = objectMapper.readTree((String) body);
        } else if (body instanceof Reader) {
            input = objectMapper.readTree((Reader) body);
        } else if (body instanceof File) {
            input = objectMapper.readTree((File) body);
        } else if (body instanceof byte[]) {
            input = objectMapper.readTree((byte[]) body);
        } else if (body instanceof InputStream) {
            input = objectMapper.readTree((InputStream) body);
        } else {
            throw new ValidationException(exchange, "Allowed body types are String, Reader, File, byte[] or InputStream.");
        }

        return input;
    }

    private Expression createExpression(final InputStream stream, final String transformSource) {
        return new Parser(new InputStreamReader(stream))
                .withFunctions(ensureFunctions())
                .withObjectFilter(ensureObjectFilter())
                .withSource(transformSource)
                .compile();
    }

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        final String result;
        try {
            result = applyExpression(exchange);
        } catch (Exception ex) {
            throw new RuntimeCamelException(ex);
        }

    }

    @Override
    public boolean matches(Exchange exchange) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(CamelContext context) {
        if (expressionText == null) {
            throw new IllegalArgumentException("expression must be specified");
        }
        final InputStream stream = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        transform = createExpression(stream, "<inline>");
    }

    private Collection<Function> ensureFunctions() {
        return Objects.requireNonNullElse(functions, Collections.emptyList());
    }

    private JsonFilter ensureObjectFilter() {
        return Objects.requireNonNullElse(objectFilter, DEFAULT_JSON_FILTER);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isMapBigDecimalAsFloats() {
        return mapBigDecimalAsFloats;
    }

    public void setMapBigDecimalAsFloats(boolean mapBigDecimalAsFloats) {
        this.mapBigDecimalAsFloats = mapBigDecimalAsFloats;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isAllowTemplateFromHeader() {
        return allowTemplateFromHeader;
    }

    public void setAllowTemplateFromHeader(boolean allowTemplateFromHeader) {
        this.allowTemplateFromHeader = allowTemplateFromHeader;
    }

    public boolean isAllowContextMapAll() {
        return allowContextMapAll;
    }

    public void setAllowContextMapAll(boolean allowContextMapAll) {
        this.allowContextMapAll = allowContextMapAll;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public JsonFilter getObjectFilter() {
        return objectFilter;
    }

    public void setObjectFilter(JsonFilter objectFilter) {
        this.objectFilter = objectFilter;
    }

    public Collection<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(Collection<Function> functions) {
        this.functions = functions;
    }

    public String getEndpointUri() {
        return URI_PREFIX + resourceUri;
    }

    public void setExpressionText(String expressionText) {
        this.expressionText = expressionText;
    }



    private static class SafeTypesOnlySerializerModifier extends BeanSerializerModifier {
        // Serialize only safe types: primitives, records, serializable objects and
        // collections/maps/arrays of them. To avoid serializing something like Response object.
        // Types that are not safe are serialized as their toString() value.
        @Override
        public JsonSerializer<?> modifySerializer(
                SerializationConfig config, BeanDescription beanDesc,
                JsonSerializer<?> serializer) {
            final Class<?> beanClass = beanDesc.getBeanClass();

            if (Collection.class.isAssignableFrom(beanClass)
                    || Map.class.isAssignableFrom(beanClass)
                    || beanClass.isArray()
                    || beanClass.isPrimitive()
                    || isRecord(beanClass)
                    || Serializable.class.isAssignableFrom(beanClass)) {
                return serializer;
            }

            return ToStringSerializer.instance;
        }

        private static boolean isRecord(Class<?> clazz) {
            final Class<?> parent = clazz.getSuperclass();
            return parent != null && parent.getName().equals("java.lang.Record");
        }
    }
}
