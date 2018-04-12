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

import io.pivotal.reactor.InteractionContext;
import io.pivotal.reactor.TestRequest;
import io.pivotal.reactor.TestResponse;
import io.pivotal.reactor.scheduler.AbstractSchedulerApiTest;
import io.pivotal.scheduler.v1.Link;
import io.pivotal.scheduler.v1.Pagination;
import io.pivotal.scheduler.v1.calls.CallResource;
import io.pivotal.scheduler.v1.calls.CreateCallRequest;
import io.pivotal.scheduler.v1.calls.CreateCallResponse;
import io.pivotal.scheduler.v1.calls.DeleteCallRequest;
import io.pivotal.scheduler.v1.calls.ListCallsRequest;
import io.pivotal.scheduler.v1.calls.ListCallsResponse;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public final class ReactorCallsTest extends AbstractSchedulerApiTest {

    private final ReactorCalls calls = new ReactorCalls(CONNECTION_CONTEXT, this.root, TOKEN_PROVIDER);

    @Test
    public void create() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(POST).path("/calls?app_guid=test-application-id")
                .payload("fixtures/scheduler/v1/calls/POST_{app_id}_request.json")
                .build())
            .response(TestResponse.builder()
                .status(CREATED)
                .payload("fixtures/scheduler/v1/calls/POST_{app_id}_response.json")
                .build())
            .build());

        this.calls
            .create(CreateCallRequest.builder()
                .applicationId("test-application-id")
                .authorizationHeader("test-authorization-header")
                .name("test-name")
                .url("test-url")
                .build())
            .as(StepVerifier::create)
            .expectNext(CreateCallResponse.builder()
                .applicationId("test-application-id")
                .authorizationHeader("test-authorization-header")
                .createdAt("test-created-at")
                .id("test-job-id")
                .name("test-name")
                .spaceId("test-space-id")
                .updatedAt("test-updated-at")
                .url("test-url")
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void delete() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(DELETE).path("/calls/test-call-id")
                .build())
            .response(TestResponse.builder()
                .status(NO_CONTENT)
                .build())
            .build());

        this.calls
            .delete(DeleteCallRequest.builder()
                .callId("test-call-id")
                .build())
            .as(StepVerifier::create)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void list() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(GET).path("/calls?space_guid=test-space-id")
                .build())
            .response(TestResponse.builder()
                .status(OK)
                .payload("fixtures/scheduler/v1/calls/GET_{space_id}_response.json")
                .build())
            .build());

        this.calls
            .list(ListCallsRequest.builder()
                .spaceId("test-space-id")
                .build())
            .as(StepVerifier::create)
            .expectNext(ListCallsResponse.builder()
                .pagination(Pagination.builder()
                    .first(Link.builder()
                        .href("test-first-link")
                        .build())
                    .last(Link.builder()
                        .href("test-last-link")
                        .build())
                    .next(Link.builder()
                        .href("test-next-link")
                        .build())
                    .previous(Link.builder()
                        .href("test-previous-link")
                        .build())
                    .totalPages(1)
                    .totalResults(1)
                    .build())
                .resource(CallResource.builder()
                    .applicationId("test-application-id")
                    .authorizationHeader("test-authorization-header")
                    .createdAt("test-created-at")
                    .id("test-job-id")
                    .name("test-name")
                    .spaceId("test-space-id")
                    .updatedAt("test-updated-at")
                    .url("test-url")
                    .build())
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

}