/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.processor.loadbalancer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.List;

/**
 * A base class for {@link LoadBalancer} implementations which choose a single destination for each exchange
 * (rather like JMS Queues)
 *
 * @version $Revision: 1.1 $
 */
public abstract class QueueLoadBalancer<E extends Exchange> extends LoadBalancerSupport<E> {

    public void process(E exchange) throws Exception {
        List<Processor<E>> list = getProcessors();
        if (list.isEmpty()) {
            throw new IllegalStateException("No processors available to process " + exchange);
        }
        Processor<E> processor = chooseProcessor(list, exchange);
        if (processor == null) {
            throw new IllegalStateException("No processors could be chosen to process " + exchange);
        }
        else {
            processor.process(exchange);
        }
    }

    protected abstract Processor<E> chooseProcessor(List<Processor<E>> processors, E exchange);
}
