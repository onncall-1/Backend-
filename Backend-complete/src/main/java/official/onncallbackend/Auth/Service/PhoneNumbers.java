package official.onncallbackend.Auth.Service;

import official.onncallbackend.common.ApiException;

/** Phone numbers are stored in one format: optional leading +, then 10 to 15 digits. */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw ApiException.badRequest("Phone number is required");
        }
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        if (!cleaned.matches("\\+?[0-9]{10,15}")) {
            throw ApiException.badRequest("Enter a valid phone number, for example +919876543210");
        }
        return cleaned;
    }
}
