package nl.vodafoneziggo.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import nl.vodafoneziggo.external.reqres.ReqresClient;
import nl.vodafoneziggo.external.reqres.ReqresUser;
import nl.vodafoneziggo.model.OrderEntity;
import nl.vodafoneziggo.orders.model.CreateOrderRequest;
import nl.vodafoneziggo.repository.OrderRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrdersApiTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReqresClient reqresClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(reqresClient.findUserByEmail(Mockito.eq("a@aa.nl"))).thenReturn(
                Optional.of(new ReqresUser(1, "a@aa.nl", "A", "Aa")));
        when(reqresClient.findUserByEmail(Mockito.eq("b@bb.nl"))).thenReturn(
                Optional.of(new ReqresUser(1, "b@bb.nl", "B", "Bb")));
    }

    @Test
    void test_createOrder_happyFlow() throws Exception {
        createOrder(123, "a@aa.nl");
        Assertions.assertEquals(1, orderRepository.count());
        OrderEntity order = orderRepository.findAll().iterator().next();
        Assertions.assertEquals(123, order.getProductID().intValue());
        Assertions.assertEquals("A", order.getFirstName());
        createOrder(456, "b@bb.nl");
        Assertions.assertEquals(2, orderRepository.count());
        order = orderRepository.findByEmail("b@bb.nl").iterator().next();
        Assertions.assertEquals(456, order.getProductID().intValue());
        Assertions.assertEquals("B", order.getFirstName());
    }

    @Test
    void test_createOrder_invalidEmail() throws Exception {
        createOrder(123, "invalid@email", status().isBadRequest(), result -> Assertions.assertEquals(
                "400 BAD_REQUEST \"Email invalid@email does not exist in external user system\"",
                Objects.requireNonNull(result.getResolvedException()).getCause().getMessage()));
    }

    @Test
    void test_createOrder_missingEmail() throws Exception {
        createOrder(123, null, status().isBadRequest(), result -> Assertions.assertTrue(
                Objects.requireNonNull(result.getResolvedException()).getMessage().contains("must not be null")));
    }

    @Test
    void test_createOrder_userDoesNotExist() throws Exception {
        createOrder(123, "c@cc.nl", status().isBadRequest(), result -> Assertions.assertEquals(
                "400 BAD_REQUEST \"Email c@cc.nl does not exist in external user system\"",
                Objects.requireNonNull(result.getResolvedException()).getCause().getMessage()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void test_getOrders_happyFlow() throws Exception {
        createOrder(123, "a@aa.nl");
        createOrder(456, "a@aa.nl");
        createOrder(789, "a@aa.nl");
        createOrder(987, "a@aa.nl");
        createOrder(765, "a@aa.nl");
        createOrder(765, "b@bb.nl");
        createOrder(321, "b@bb.nl");
        List<OrderEntity> result = objectMapper.readValue(mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), List.class);
        Assertions.assertEquals(7, result.size());
        result = objectMapper.readValue(mockMvc.perform(get("/api/orders?email=a@aa.nl"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), List.class);
        Assertions.assertEquals(5, result.size());
        result = objectMapper.readValue(mockMvc.perform(get("/api/orders?email=b@bb.nl"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(), List.class);
        Assertions.assertEquals(2, result.size());
    }

    private void createOrder(Integer orderId, String mail) throws Exception {
        createOrder(orderId, mail, status().isCreated(), result -> Assertions.assertTrue(true));
    }

    private void createOrder(Integer orderId, String mail, ResultMatcher status, ResultMatcher resultMatcher)
            throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(orderId, mail);
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status).andExpect(resultMatcher);
    }
}
