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

package io.pivotal.reactor.scheduler.v1.jobs;

import io.pivotal.reactor.scheduler.v1.AbstractSchedulerV1Operations;
import io.pivotal.scheduler.v1.jobs.CreateJobRequest;
import io.pivotal.scheduler.v1.jobs.CreateJobResponse;
import io.pivotal.scheduler.v1.jobs.Jobs;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import reactor.core.publisher.Mono;

/**
 * The Reactor-based implementation of {@link Jobs}
 */
public class ReactorJobs extends AbstractSchedulerV1Operations implements Jobs {

    /**
     * Creates an instance
     *
     * @param connectionContext the {@link ConnectionContext} to use when communicating with the server
     * @param root              the root URI of the server. Typically something like {@code https://api.run.pivotal.io}.
     * @param tokenProvider     the {@link TokenProvider} to use when communicating with the server
     */
    public ReactorJobs(ConnectionContext connectionContext, Mono<String> root, TokenProvider tokenProvider) {
        super(connectionContext, root, tokenProvider);
    }

    @Override
    public Mono<CreateJobResponse> create(CreateJobRequest request) {
        return post(request, CreateJobResponse.class, builder -> builder.pathSegment("jobs"))
            .checkpoint();
    }

}
