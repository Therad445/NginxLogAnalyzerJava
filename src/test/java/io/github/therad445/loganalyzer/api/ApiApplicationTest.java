package io.github.therad445.loganalyzer.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainStartsApplication() {
        ApiApplication.main(new String[] {});
    }
}
