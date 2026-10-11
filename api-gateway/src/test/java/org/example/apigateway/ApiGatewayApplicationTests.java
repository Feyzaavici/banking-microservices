package org.example.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=test-only-jwt-secret-at-least-32-bytes-long",
        "eureka.client.enabled=false"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
