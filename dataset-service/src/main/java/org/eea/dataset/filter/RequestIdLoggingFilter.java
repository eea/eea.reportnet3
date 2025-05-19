package org.eea.dataset.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdLoggingFilter implements Filter {

    private static final String HEADER = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            String reqId = httpReq.getHeader(HEADER);
            if (reqId == null || reqId.isBlank()) {
                String method = ((HttpServletRequest) request).getMethod();
                String path = ((HttpServletRequest) request).getRequestURI();
                String pathSignature = method + "-" + path.replaceAll("/", "-");
                String hash = UUID.randomUUID().toString().substring(0, 6); // short hash
                reqId = pathSignature + "-" + hash;
            }
            MDC.put("reqId", reqId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove("reqId");
        }
    }
}
