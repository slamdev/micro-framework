package com.github.slamdev.microframework.http.server.handlers;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.FilterReply;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.ExchangeCompletionListener.NextListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HttpString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class LogbackAccessHandler implements HttpHandler {

    private final HttpHandler nextHandler;

    private final LogbackAccessContext context = new LogbackAccessContext();

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.addExchangeCompleteListener(this::handleExchangeEvent);
        nextHandler.handleRequest(exchange);
    }

    private void handleExchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        IAccessEvent event = new AccessEvent(exchange);
        context.emit(event);
        nextListener.proceed();
    }

    private static class LogbackAccessContext extends AccessContext {

        private static final List<String> DEFAULT_CONFIGS = unmodifiableList(asList(
                "logback-access-test.xml",
                "logback-access.xml"
        ));

        private static final String FALLBACK_CONFIG = "logback-access-default.xml";

        LogbackAccessContext() {
            setName(CoreConstants.DEFAULT_CONTEXT_NAME);
            for (String config : DEFAULT_CONFIGS) {
                if (configureIfPresent(config)) {
                    return;
                }
            }
            configure(FALLBACK_CONFIG);
            start();
        }

        private void configure(String config) {
            try {
                configureWithCauseThrowing(config);
            } catch (IOException | JoranException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private boolean configureIfPresent(String config) {
            try {
                configureWithCauseThrowing(config);
                return true;
            } catch (FileNotFoundException exc) {
                return false;
            } catch (IOException | JoranException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private void configureWithCauseThrowing(String config) throws IOException, JoranException {
            URL url = getClass().getClassLoader().getResource(config);
            if (url == null) {
                throw new FileNotFoundException(config);
            }
            try (InputStream stream = url.openStream()) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(this);
                configurator.doConfigure(stream);
            }
        }

        void emit(IAccessEvent event) {
            if (getFilterChainDecision(event) != FilterReply.DENY) {
                callAppenders(event);
            }
        }
    }

    @Getter
    private static class AccessEvent implements IAccessEvent {
        private final HttpServerExchange exchange;

        private final long timeStamp = System.currentTimeMillis();

        private final long elapsedTime;

        @Setter
        private String threadName;

        AccessEvent(HttpServerExchange exchange) {
            this.exchange = exchange;
            this.elapsedTime = calculateElapsedTime();
            setThreadName(Thread.currentThread().getName());
        }

        private long calculateElapsedTime() {
            long currentTimeMillis = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            long requestStartTime = exchange.getRequestStartTime();
            long requestTimestamp = currentTimeMillis - NANOSECONDS.toMillis(nanoTime - requestStartTime);
            if (requestTimestamp < 0) {
                return -1;
            }
            return timeStamp - requestTimestamp;
        }

        @Override
        public javax.servlet.http.HttpServletRequest getRequest() {
            return null;
        }

        @Override
        public javax.servlet.http.HttpServletResponse getResponse() {
            return null;
        }

        @Override
        public long getElapsedSeconds() {
            return elapsedTime < 0 ? elapsedTime : elapsedTime / 1000;
        }

        @Override
        public String getRequestURI() {
            return exchange.getRequestURI();
        }

        @Override
        public String getRequestURL() {
            return exchange.getRequestURL();
        }

        @Override
        public String getRemoteHost() {
            InetSocketAddress sourceAddress = exchange.getSourceAddress();
            if (sourceAddress == null) {
                return "";
            }
            return sourceAddress.getHostString();
        }

        @Override
        public String getRemoteUser() {
            SecurityContext securityContext = exchange.getSecurityContext();
            Principal userPrincipal = null;
            Account account;
            if (securityContext != null && (account = securityContext.getAuthenticatedAccount()) != null) {
                userPrincipal = account.getPrincipal();
            }
            return userPrincipal != null ? userPrincipal.getName() : null;
        }

        @Override
        public String getProtocol() {
            return exchange.getProtocol().toString();
        }

        @Override
        public String getMethod() {
            return exchange.getRequestMethod().toString();
        }

        @Override
        public String getServerName() {
            return exchange.getHostName();
        }

        @Override
        public String getSessionID() {
            return null;
        }

        @Override
        public String getQueryString() {
            return exchange.getQueryString();
        }

        @Override
        public String getRemoteAddr() {
            return exchange.getSourceAddress().toString();
        }

        @Override
        public String getRequestHeader(String key) {
            return exchange.getRequestHeaders().getFirst(key);
        }

        @Override
        public Enumeration<String> getRequestHeaderNames() {
            return enumeration(exchange.getRequestHeaders().getHeaderNames().stream().map(HttpString::toString).collect(toList()));
        }

        @Override
        public Map<String, String> getRequestHeaderMap() {
            Map<String, String> headers = new HashMap<>();
            exchange.getRequestHeaders().forEach(h -> headers.put(h.getHeaderName().toString(), h.getFirst()));
            return headers;
        }

        @Override
        public Map<String, String[]> getRequestParameterMap() {
            return exchange.getQueryParameters().entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> e.getValue().toArray(new String[]{})));
        }

        @Override
        public String getAttribute(String key) {
            return null;
        }

        @Override
        public String[] getRequestParameter(String key) {
            return exchange.getQueryParameters().get(key).toArray(new String[]{});
        }

        @Override
        public String getCookie(String key) {
            Cookie cookie = exchange.getRequestCookies().getOrDefault(key, exchange.getResponseCookies().get(key));
            return cookie == null ? null : cookie.getName();
        }

        @Override
        public long getContentLength() {
            return exchange.getResponseBytesSent();
        }

        @Override
        public int getStatusCode() {
            return exchange.getStatusCode();
        }

        @Override
        public String getRequestContent() {
            return null;
        }

        @Override
        public String getResponseContent() {
            return null;
        }

        @Override
        public int getLocalPort() {
            SocketAddress address = exchange.getConnection().getLocalAddress();
            if (address instanceof InetSocketAddress) {
                return ((InetSocketAddress) address).getPort();
            }
            return -1;
        }

        @Override
        public ServerAdapter getServerAdapter() {
            return null;
        }

        @Override
        public String getResponseHeader(String key) {
            return exchange.getResponseHeaders().getFirst(key);
        }

        @Override
        public Map<String, String> getResponseHeaderMap() {
            Map<String, String> headers = new HashMap<>();
            exchange.getResponseHeaders().forEach(h -> headers.put(h.getHeaderName().toString(), h.getFirst()));
            return headers;
        }

        @Override
        public List<String> getResponseHeaderNameList() {
            return exchange.getResponseHeaders().getHeaderNames().stream().map(HttpString::toString).collect(toList());
        }

        @Override
        public void prepareForDeferredProcessing() {
        }
    }
}
