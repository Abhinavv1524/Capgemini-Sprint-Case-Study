package in.cg.skillsync.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class CorsPreflightWebFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String origin = exchange.getRequest().getHeaders().getOrigin();
        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(method)
                && origin != null
                && (origin.equals("http://localhost:4200") || origin.equals("http://127.0.0.1:4200"))) {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set("Access-Control-Allow-Origin", origin);
            headers.set("Vary", "Origin");
            headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Authorization,Content-Type,X-Requested-With");
            headers.set("Access-Control-Allow-Credentials", "true");
            headers.set("Access-Control-Max-Age", "3600");
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
