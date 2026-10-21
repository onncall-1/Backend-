package official.onncallbackend.Auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Every test uses its own phone number, because the database is shared across tests. */
    private String newPhone() {
        return "+9199" + ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999);
    }

    private String requestOtp(String phone) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devOtp").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("devOtp").asText();
    }

    private String registerClient(String phone, String otp) throws Exception {
        String body = "{\"phone\":\"" + phone + "\",\"otp\":\"" + otp
                + "\",\"role\":\"CLIENT\",\"name\":\"Asha\"}";
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    @Test
    void clientRegistersAndSeesOwnProfile() throws Exception {
        String phone = newPhone();
        String otp = requestOtp(phone);
        String token = registerClient(phone, otp);

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.profileId").isNumber());
    }

    @Test
    void loginForUnknownPhoneReturnsNotFoundAndKeepsOtpUsable() throws Exception {
        String phone = newPhone();
        String otp = requestOtp(phone);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"otp\":\"" + otp + "\"}"))
                .andExpect(status().isNotFound());

        // The same code is still valid for registration, because login did not consume it.
        registerClient(phone, otp);
    }

    @Test
    void wrongOtpIsRejected() throws Exception {
        String phone = newPhone();
        String otp = requestOtp(phone);
        String wrong = otp.equals("000000") ? "111111" : "000000";

        String body = "{\"phone\":\"" + phone + "\",\"otp\":\"" + wrong
                + "\",\"role\":\"CLIENT\",\"name\":\"Ravi\"}";
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect code"));
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void clientCannotUseAdminEndpoint() throws Exception {
        String phone = newPhone();
        String token = registerClient(phone, requestOtp(phone));

        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void anyoneCanReadServiceCatalogue() throws Exception {
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk());
    }

    /** Firebase endpoints, using the dev token verifier (app.auth.provider=dev in the test profile). */
    @Test
    void firebaseRegisterThenLoginWithVerifiedPhone() throws Exception {
        String phone = newPhone();
        String idToken = "dev-token:" + phone;

        mvc.perform(post("/api/auth/firebase/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + idToken + "\"}"))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/auth/firebase/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + idToken + "\",\"role\":\"PROVIDER\",\"name\":\"Imran\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("PROVIDER"))
                .andExpect(jsonPath("$.user.phone").value(phone));

        mvc.perform(post("/api/auth/firebase/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"" + idToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void invalidFirebaseTokenIsRejected() throws Exception {
        mvc.perform(post("/api/auth/firebase/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"not-a-real-token\"}"))
                .andExpect(status().isUnauthorized());
    }
}
