package nl.vodafoneziggo.repository;

import nl.vodafoneziggo.model.OrderEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testPersist() {
        createOrder(1, "a@aa.nl", "Aadje", "Aa");
        Assertions.assertEquals(1, orderRepository.count());
    }

    @Test
    void test_findByEmailAndProductID() {
        createOrder(1, "c@cc.nl", "Ceesje", "Cc");
        createOrder(1, "d@dd.nl", "Deedje", "Dd");
        createOrder(2, "d@dd.nl", "Deedje", "Dd");
        Assertions.assertEquals(3, orderRepository.count());
        Assertions.assertTrue(orderRepository.findByEmailAndProductID("c@cc.nl", 1).isPresent());
        Assertions.assertTrue(orderRepository.findByEmailAndProductID("d@dd.nl", 2).isPresent());
        Assertions.assertTrue(orderRepository.findByEmailAndProductID("c@cc.nl", 2).isEmpty());
    }

    @Test
    void test_findByEmail() {
        createOrder(1, "c@cc.nl", "Ceesje", "Cc");
        createOrder(1, "d@dd.nl", "Deedje", "Dd");
        createOrder(2, "d@dd.nl", "Deedje", "Dd");
        Assertions.assertEquals(3, orderRepository.count());
        List<OrderEntity> cByEmail = (List<OrderEntity>) orderRepository.findByEmail("c@cc.nl");
        Assertions.assertEquals(1, cByEmail.size());
        List<OrderEntity> dByEmail = (List<OrderEntity>) orderRepository.findByEmail("d@dd.nl");
        Assertions.assertEquals(2, dByEmail.size());
    }

    private void createOrder(Integer productID, String email, String firstName, String lastName) {
        OrderEntity order = new OrderEntity();
        order.setProductID(productID);
        order.setEmail(email);
        order.setFirstName(firstName);
        order.setLastName(lastName);
        orderRepository.save(order);
    }
}
