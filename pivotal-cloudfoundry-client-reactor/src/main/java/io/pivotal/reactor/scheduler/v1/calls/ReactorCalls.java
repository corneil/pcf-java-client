/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.reactor.scheduler.v1.calls;

import io.pivotal.reactor.scheduler.v1.AbstractSchedulerV1Operations;
import io.pivotal.scheduler.v1.calls.Calls;
import io.pivotal.scheduler.v1.calls.CreateCallRequest;
import io.pivotal.scheduler.v1.calls.CreateCallResponse;
import io.pivotal.scheduler.v1.calls.DeleteCallRequest;
import io.pivotal.scheduler.v1.calls.ListCallsRequest;
import io.pivotal.scheduler.v1.calls.ListCallsResponse;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import reactor.core.publisher.Mono;

/**
 * The Reactor-based implementation of {@link Calls}
 */
public class ReactorCalls extends AbstractSchedulerV1Operations implements Calls {

    /**
     * Creates an instance
     *
     * @param connectionContext the {@link ConnectionContext} to use when communicating with the server
     * @param root              the root URI of the server. Typically something like {@code https://api.run.pivotal.io}.
     * @param tokenProvider     the {@link TokenProvider} to use when communicating with the server
     */
    public ReactorCalls(ConnectionContext connectionContext, Mono<String> root, TokenProvider tokenProvider) {
        super(connectionContext, root, tokenProvider);
    }

    @Override
    public Mono<CreateCallResponse> create(CreateCallRequest request) {
        return post(request, CreateCallResponse.class, builder -> builder.pathSegment("calls"))
            .checkpoint();
    }

    @Override
    public Mono<Void> delete(DeleteCallRequest request) {
        return delete(request, Void.class, builder -> builder.pathSegment("calls", request.getCallId()))
            .checkpoint();
    }

    @Override
    public Mono<ListCallsResponse> list(ListCallsRequest request) {
        return get(request, ListCallsResponse.class, builder -> builder.pathSegment("calls"))
            .checkpoint();
    }

}