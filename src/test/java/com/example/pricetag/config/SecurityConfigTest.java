package com.example.pricetag.config;

import com.example.pricetag.features.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/** Exercises the real security filter chain without a database or external services. */
@SpringJUnitConfig(SecurityConfigTest.TestWebConfiguration.class)
@WebAppConfiguration
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /api/v1/category/add",
            "PATCH, /api/v1/category/update/1",
            "PATCH, /api/v1/category/update/image",
            "DELETE, /api/v1/category/1",
            "POST, /api/v1/subcategory/1/add",
            "PATCH, /api/v1/subcategory/edit",
            "DELETE, /api/v1/subcategory/1",
            "POST, /api/v1/product/category/1/subcategory/2/add",
            "PATCH, /api/v1/product/1",
            "DELETE, /api/v1/product/1",
            "POST, /api/v1/variant/product/1",
            "PATCH, /api/v1/variant/edit/1",
            "DELETE, /api/v1/variant/"
    })
    void catalogWritesRequireAdmin(String method, String path) throws Exception {
        mvc.perform(endpoint(method, path)).andExpect(status().is4xxClientError());
        mvc.perform(endpoint(method, path).with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(endpoint(method, path).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /api/v1/category/",
            "GET, /api/v1/subcategory",
            "GET, /api/v1/subcategory/1",
            "GET, /api/v1/product",
            "GET, /api/v1/product/1",
            "GET, /api/v1/product/subcategory/1",
            "GET, /api/v1/product/subcategory/1/search",
            "GET, /api/v1/variant/",
            "GET, /api/v1/variant/1",
            "GET, /api/v1/variant/product/1",
            "HEAD, /api/v1/variant/product/1",
            "GET, /api/v1/user/",
            "PATCH, /api/v1/user/edit",
            "GET, /api/v1/cart",
            "POST, /api/v1/cart/items",
            "PATCH, /api/v1/cart/item/1",
            "DELETE, /api/v1/cart/item/1",
            "DELETE, /api/v1/cart",
            "GET, /api/v1/address",
            "POST, /api/v1/address",
            "PATCH, /api/v1/address/1",
            "PATCH, /api/v1/address/1/default",
            "DELETE, /api/v1/address/1",
            "DELETE, /api/v1/address/delete_addresses",
            "GET, /api/v1/orders",
            "POST, /api/v1/orders",
            "GET, /api/v1/orders/PT-TEST",
            "POST, /api/v1/orders/PT-TEST/cancel",
            "POST, /api/v1/orders/PT-TEST/payment-intent",
            "POST, /api/v1/rating-review/add/product/1",
            "GET, /api/v1/rating-review/1",
            "GET, /api/v1/rating-review/product/1",
            "GET, /api/v1/rating-review/user/product/1",
            "GET, /api/v1/rating-review/user",
            "PATCH, /api/v1/rating-review/update/1",
            "DELETE, /api/v1/rating-review/1"
    })
    void customerEndpointsRequireUserOrAdmin(String method, String path) throws Exception {
        mvc.perform(endpoint(method, path)).andExpect(status().is4xxClientError());
        mvc.perform(endpoint(method, path).with(user("customer").roles("USER")))
                .andExpect(status().isOk());
        mvc.perform(endpoint(method, path).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(endpoint(method, path).with(user("other").roles("OTHER")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /",
            "GET, /api/v1/auth/welcome",
            "GET, /actuator/health",
            "GET, /swagger-ui/index.html",
            "GET, /v3/api-docs",
            "POST, /api/v1/auth/login",
            "POST, /api/v1/auth/register",
            "POST, /api/v1/auth/verify-otp",
            "POST, /api/v1/auth/forgot-password",
            "PATCH, /api/v1/auth/verify-forgot-password-otp",
            "POST, /api/v1/auth/refreshToken",
            "POST, /api/v1/payments/stripe/webhook"
    })
    void publicEndpointsReachHandlerWithoutAccessToken(String method, String path) throws Exception {
        // Handler is a stub: refresh-token and webhook-signature validation remain service concerns.
        mvc.perform(endpoint(method, path)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /api/v1/new-feature",
            "POST, /api/v1/image/upload",
            "GET, /user/profile",
            "DELETE, /api/v1/orders/PT-TEST",
            "GET, /api/v1/orders/PT-TEST/payment-intent",
            "GET, /api/v1/payments/stripe/webhook",
            "GET, /api/v1/auth/refreshToken",
            "POST, /api/v1/auth/verify-forgot-password-otp"
    })
    void unlistedRoutesAndMethodsAreDeniedEvenForAdmin(String method, String path) throws Exception {
        mvc.perform(endpoint(method, path).with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void configuredOriginCanPreflightPaymentInitializationWithoutLogin() throws Exception {
        mvc.perform(options("/api/v1/orders/PT-TEST/payment-intent")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    private MockHttpServletRequestBuilder endpoint(String method, String path) {
        return request(HttpMethod.valueOf(method), path);
    }

    @TestConfiguration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtFilter.class, EndpointStub.class})
    static class TestWebConfiguration {
        @Bean
        UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @RestController
    static class EndpointStub {
        @RequestMapping("/**")
        String endpoint() {
            return "allowed";
        }
    }
}
