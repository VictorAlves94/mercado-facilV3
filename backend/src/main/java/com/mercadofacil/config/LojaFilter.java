package com.mercadofacil.config;

import jakarta.servlet.Filter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class LojaFilter implements Filter {
    private static final String HEADER = "X-Loja-Id";

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/",
            "/api/v1/lojas",
            "/actuator",
            "/h2-console"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Liberar rotas públicas
        String path = request.getRequestURI();
        for (String pub : PUBLIC_PATHS) {
            if (path.startsWith(pub)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // OPTIONS preflight — nunca bloquear
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String lojaIdStr = request.getHeader(HEADER);

        if (lojaIdStr == null || lojaIdStr.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"erro\": \"Header X-Loja-Id é obrigatório.\"}"
            );
            return;
        }

        try {
            Long lojaId = Long.parseLong(lojaIdStr.trim());
            LojaContext.set(lojaId);
            chain.doFilter(req, res);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"erro\": \"X-Loja-Id deve ser um número válido.\"}"
            );
        } finally {
            LojaContext.clear(); // CRÍTICO: evitar memory leak
        }
    }
}
