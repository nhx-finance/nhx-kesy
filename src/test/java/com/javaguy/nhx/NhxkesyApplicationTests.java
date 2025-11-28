package com.javaguy.nhx;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Integration test - full context loading tested elsewhere")
class NhxkesyApplicationTests {

    @Test
    void contextLoads() {
    }

}
