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
package org.apache.camel.component.mail;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mail.Mailbox.MailboxUser;
import org.apache.camel.component.mail.Mailbox.Protocol;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test to verify that the polling consumer delivers an empty Exchange when the sendEmptyMessageWhenIdle property is set
 * and a polling event yields no results.
 */
public class MailConsumerIdleMessageTest extends CamelTestSupport {
    @SuppressWarnings({ "checkstyle:ConstantName" })
    private static final MailboxUser james = Mailbox.getOrCreateUser("james", "secret");

    @Test
    public void testConsumeIdleMessages() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(2);
        MockEndpoint.assertIsSatisfied(context);
        assertNull(mock.getExchanges().get(0).getIn().getBody());
        assertNull(mock.getExchanges().get(1).getIn().getBody());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(james.uriPrefix(Protocol.pop3) + "&initialDelay=100&delay=100&sendEmptyMessageWhenIdle=true")
                        .to("mock:result");
            }
        };
    }

}
