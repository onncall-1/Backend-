package official.onncallbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnnCallBackendApplication {

    public static void main(String[] args) {
        if ("Asia/Calcutta".equalsIgnoreCase(java.util.TimeZone.getDefault().getID())) {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
        }
        SpringApplication.run(OnnCallBackendApplication.class, args);
    }
}
