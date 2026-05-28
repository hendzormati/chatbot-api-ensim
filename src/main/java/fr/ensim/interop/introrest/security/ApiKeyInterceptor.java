package fr.ensim.interop.introrest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ensim.interop.introrest.model.generated.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${api.access.token}")
    private String apiToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String headerToken = request.getHeader("X-API-KEY");
        if (apiToken == null || !apiToken.equals(headerToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse()
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error("Unauthorized")
                    .message("Token API manquant ou invalide");
            response.getWriter().write(new ObjectMapper().writeValueAsString(error));
            return false;
        }
        return true;
    }
}