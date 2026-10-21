package official.onncallbackend.Auth;

import official.onncallbackend.Auth.Service.PhoneNumbers;
import official.onncallbackend.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumbersTest {

    @Test
    void stripsSpacesAndDashes() {
        assertEquals("+919876543210", PhoneNumbers.normalize("+91 98765-43210"));
    }

    @Test
    void rejectsShortNumbers() {
        ApiException ex = assertThrows(ApiException.class, () -> PhoneNumbers.normalize("12345"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
