package nl.vodafoneziggo.controller;

import nl.vodafoneziggo.external.reqres.ReqresClient;
import nl.vodafoneziggo.external.reqres.ReqresUser;
import nl.vodafoneziggo.model.OrderEntity;
import nl.vodafoneziggo.orders.model.CreateOrderRequest;
import nl.vodafoneziggo.repository.OrderRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        when(reqresClient.findUserByEmail(Mockito.anyString())).thenReturn(Optional.of(new ReqresUser(1, "a@aa.nl", "A", "Aa")));
    }

    @Test
    void test_createOrder_happyFlow() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(123, "a@aa.nl");
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        Assertions.assertEquals(1, orderRepository.count());
        OrderEntity order = orderRepository.findAll().iterator().next();
        Assertions.assertEquals(123, order.getProductID().intValue());
        Assertions.assertEquals("A", order.getFirstName());
    }
}
