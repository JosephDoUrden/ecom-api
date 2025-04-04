package com.ecom.api;

import com.ecom.api.config.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfiguration.class)
class EcommerceApplicationTests {

  @Test
  void contextLoads() {
    // Simple test to verify the application context loads successfully
  }

}
