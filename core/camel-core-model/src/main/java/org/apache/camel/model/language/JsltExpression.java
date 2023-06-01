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
package org.apache.camel.model.language;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.camel.spi.Metadata;

/**
 * Evaluates a JSLT expression against a JSON message body.
 */
@Metadata(firstVersion = "3.21.0", label = "language,json", title = "JSLT")
@XmlRootElement(name = "jslt")
@XmlAccessorType(XmlAccessType.FIELD)
public class JsltExpression extends SingleInputTypedExpressionDefinition {

    public JsltExpression() {
    }

    public JsltExpression(String expression) {
        super(expression);
    }

    private JsltExpression(Builder builder) {
        super(builder);
    }

    @Override
    public String getLanguage() {
        return "jslt";
    }

    /**
     * {@code Builder} is a specific builder for {@link JsltExpression}.
     */
    @XmlTransient
    public static class Builder extends AbstractBuilder<Builder, JsltExpression> {

        @Override
        public JsltExpression end() {
            return new JsltExpression(this);
        }
    }
}
