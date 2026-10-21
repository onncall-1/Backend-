package official.onncallbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OnnCallBackendApplicationTests {

    /** Fails if any bean, entity mapping, or security rule cannot be wired at startup. */
    @Test
    void contextLoads() {
    }
}
