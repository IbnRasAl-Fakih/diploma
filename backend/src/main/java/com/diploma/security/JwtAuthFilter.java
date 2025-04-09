package com.diploma.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.List;

@Component
public class JwtAuthFilter implements Filter {

    private static final List<String> PUBLIC_PATH_CONTAINS = List.of(
        "/swagger-ui",
        "/v3/api-docs",
        "/swagger-resources",
        "/webjars"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        String path = http.getRequestURI();

        // ✅ если path содержит что-то из разрешённых сегментов — пропускаем
        boolean isPublic = PUBLIC_PATH_CONTAINS.stream().anyMatch(path::contains);
        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = http.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        try {
            String token = authHeader.substring(7);
            UUID userId = UUID.fromString(token); // замени на свою декодировку
            http.setAttribute("userId", userId);
        } catch (Exception e) {
            httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        chain.doFilter(request, response);
    }
}
