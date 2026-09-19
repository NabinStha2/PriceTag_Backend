package com.example.pricetag;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.auth.service.JwtService;
import com.example.pricetag.features.payment.dto.responseDto.InitializePaymentResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(properties = "spring.config.import=")
@ActiveProfiles("test")
class PricetagApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtService jwtService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void openApiDescribesCheckoutAndWebhookEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/orders'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders/{orderNumber}/payment-intent'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders/{orderNumber}/cancel'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/payments/stripe/webhook'].post").exists());
    }

    @Test
    void malformedBearerTokenKeepsExistingJsonErrorContract() throws Exception {
        mvc.perform(get("/api/v1/cart").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid token"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void jwtSigningAndParsingStillWorkWithJacksonDependencyChanges() {
        String email = "migration-test@example.com";
        String token = jwtService.generateToken(email);
        var user = User.withUsername(email).password("unused").roles("USER").build();

        assertThat(jwtService.extractUserName(token)).isEqualTo(email);
        assertThat(jwtService.validateToken(token, user)).isTrue();
    }

    @Test
    void paymentResponseKeepsMobileFieldNamesAmountsAndIsoDates() {
        var payment = InitializePaymentResponseDto.builder()
                .orderNumber("PT-TEST")
                .paymentIntentId("pi_test")
                .clientSecret("pi_test_secret_placeholder")
                .amount(13250L)
                .currency("aud")
                .expiresAt(LocalDateTime.of(2026, 9, 20, 12, 30))
                .build();
        var response = CommonResponseDto.builder()
                .success(true)
                .status(200)
                .data(payment)
                .build();

        var json = jsonMapper.readTree(jsonMapper.writeValueAsString(response));
        assertThat(json.at("/data/orderNumber").asString()).isEqualTo("PT-TEST");
        assertThat(json.at("/data/paymentIntentId").asString()).isEqualTo("pi_test");
        assertThat(json.at("/data/clientSecret").asString()).isEqualTo("pi_test_secret_placeholder");
        assertThat(json.at("/data/amount").isIntegralNumber()).isTrue();
        assertThat(json.at("/data/amount").asLong()).isEqualTo(13250L);
        assertThat(json.at("/data/currency").asString()).isEqualTo("aud");
        assertThat(json.at("/data/expiresAt").asString()).isEqualTo("2026-09-20T12:30:00");
        assertThat(json.has("pagination")).isFalse();
    }

    @Test
    void stripeWebhookStillRequiresASignature() throws Exception {
        mvc.perform(post("/api/v1/payments/stripe/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                .andExpect(status().isBadRequest());
    }

}
