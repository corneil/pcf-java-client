/*
 * Copyright 2018-2019 the original author or authors.
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

package io.pivotal.reactor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.UnknownSchedulerException;
import io.pivotal.scheduler.v1.SchedulerError;
import io.pivotal.scheduler.v1.SchedulerException;
import org.cloudfoundry.reactor.HttpClientResponseWithBody;
import org.cloudfoundry.reactor.HttpClientResponseWithConnection;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.http.client.HttpClientResponse;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ErrorPayloadMapperTest {

    private final Connection connection = mock(Connection.class, RETURNS_SMART_NULLS);

    private final NettyInbound inbound = mock(NettyInbound.class, RETURNS_SMART_NULLS);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClientResponse response = mock(HttpClientResponse.class, RETURNS_SMART_NULLS);

    @Test
    public void schedulerBadPayload() throws IOException {
        when(this.connection.inbound()).thenReturn(this.inbound);
        when(this.inbound.receive()).thenReturn(ByteBufFlux.fromPath(new ClassPathResource("fixtures/invalid_error_response.json").getFile().toPath()));
        when(this.response.status()).thenReturn(BAD_REQUEST);
        HttpClientResponseWithConnection responseWithConnection = buildResponseWithConnection(this.connection);

        Flux.just(responseWithConnection)
            .transform(ErrorPayloadMappers.scheduler(this.objectMapper))
            .as(StepVerifier::create)
            .consumeErrorWith(t -> assertThat(t)
                .isInstanceOf(UnknownSchedulerException.class)
                .hasMessage("Unknown Scheduler Exception")
                .extracting("statusCode", "payload")
                .containsExactly(BAD_REQUEST.code(), "Invalid Error Response"))
            .verify(Duration.ofSeconds(1));
    }

    @Test
    public void schedulerClientError() throws IOException {
        when(this.connection.inbound()).thenReturn(this.inbound);
        when(this.inbound.receive()).thenReturn(ByteBufFlux.fromPath(new ClassPathResource("fixtures/scheduler/v1/error_response.json").getFile().toPath()));
        when(this.response.status()).thenReturn(BAD_REQUEST);
        HttpClientResponseWithConnection responseWithConnection = buildResponseWithConnection(this.connection);

        Flux.just(responseWithConnection)
            .transform(ErrorPayloadMappers.scheduler(this.objectMapper))
            .as(StepVerifier::create)
            .consumeErrorWith(t -> {
                SchedulerError error = SchedulerError.builder()
                    .resource("scheduleRequest")
                    .message("The cron expression 'a b c d e f' is invalid.")
                    .build();

                assertThat(t)
                    .isInstanceOf(SchedulerException.class)
                    .hasMessage("Validation of resource failed.")
                    .extracting("statusCode", "description", "errors")
                    .containsExactly(BAD_REQUEST.code(), "Validation of resource failed.", Collections.singletonList(error));
            })
            .verify(Duration.ofSeconds(1));
    }

    @Test
    public void schedulerNoError() {
        when(this.response.status()).thenReturn(OK);
        HttpClientResponseWithConnection responseWithConnection = buildResponseWithConnection(this.connection);

        Flux.just(responseWithConnection)
            .transform(ErrorPayloadMappers.scheduler(this.objectMapper))
            .as(StepVerifier::create)
            .expectNext(responseWithConnection)
            .expectComplete()
            .verify(Duration.ofSeconds(1));
    }

    @Test
    public void schedulerServerError() throws IOException {
        when(this.connection.inbound()).thenReturn(this.inbound);
        when(this.inbound.receive()).thenReturn(ByteBufFlux.fromPath(new ClassPathResource("fixtures/scheduler/v1/error_response.json").getFile().toPath()));
        when(this.response.status()).thenReturn(INTERNAL_SERVER_ERROR);
        HttpClientResponseWithConnection responseWithConnection = buildResponseWithConnection(this.connection);

        Flux.just(responseWithConnection)
            .transform(ErrorPayloadMappers.scheduler(this.objectMapper))
            .as(StepVerifier::create)
            .consumeErrorWith(t -> {
                SchedulerError error = SchedulerError.builder()
                    .resource("scheduleRequest")
                    .message("The cron expression 'a b c d e f' is invalid.")
                    .build();

                assertThat(t)
                    .isInstanceOf(SchedulerException.class)
                    .hasMessage("Validation of resource failed.")
                    .extracting("statusCode", "description", "errors")
                    .containsExactly(INTERNAL_SERVER_ERROR.code(), "Validation of resource failed.", Collections.singletonList(error));
            })
            .verify(Duration.ofSeconds(1));
    }

    private HttpClientResponseWithBody buildResponseWithBody() {
        return buildResponseWithBody(ByteBufFlux.fromInbound(Flux.empty()));
    }

    private HttpClientResponseWithBody buildResponseWithBody(ByteBufFlux body) {
        return HttpClientResponseWithBody.of(body, this.response);
    }

    private HttpClientResponseWithConnection buildResponseWithConnection(Connection connection) {
        return HttpClientResponseWithConnection.of(connection, this.response);
    }

}