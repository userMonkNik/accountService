package account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Map<String, Object> errorData = new LinkedHashMap<>();
        final String msg = "Access Denied!";
        HttpStatus status = HttpStatus.FORBIDDEN;

        errorData.put("timestamp", LocalDateTime.now().toString());
        errorData.put("status", status.value());
        errorData.put("error", status.getReasonPhrase());
        errorData.put("message", msg);
        errorData.put("path", request.getRequestURI());

        response.setStatus(status.value());
        response.getOutputStream()
                .println(
                        mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(errorData)
                );
    }
}
