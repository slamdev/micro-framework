package com.github.slamdev.microframework.http.server.responders;

import com.squareup.moshi.Moshi;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static io.undertow.util.Headers.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;

@UtilityClass
public class Responders {

    public void sendError(HttpServerExchange exchange, int statusCode, Throwable throwable) {
        exchange.setStatusCode(statusCode);
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", Instant.now().toEpochMilli());
        data.put("status", statusCode);
        data.put("error", StatusCodes.getReason(statusCode));
        data.put("message", throwable.getMessage());
        data.put("path", exchange.getRequestPath());
        sendJson(exchange, data);
    }

    public void sendJson(HttpServerExchange exchange, Object object) {
        Moshi moshi = new Moshi.Builder().build();
        String string = moshi.adapter(Object.class).toJson(object);
        sendJson(exchange, string);
    }

    public void sendJson(HttpServerExchange exchange, String string) {
        exchange.getResponseHeaders().put(CONTENT_TYPE, JSON_UTF_8.toString());
        exchange.getResponseSender().send(ByteBuffer.wrap(string.getBytes(UTF_8)));
    }
}
