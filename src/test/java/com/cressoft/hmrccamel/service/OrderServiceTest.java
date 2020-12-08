package com.cressoft.hmrccamel.service;

import com.cressoft.hmrccamel.data.*;
import com.cressoft.hmrccamel.exception.OrderNotFoundException;
import com.cressoft.hmrccamel.model.Order;
import com.cressoft.hmrccamel.repo.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    public void shouldFindAllOrders() {
        List<Order> order = newArrayList(
                new Order("o1", "1", "p1", 1),
                new Order("o2", "2", "p2", 1));
        given(orderRepository.findAll()).willReturn(order);

        OrderDataList actual = orderService.findAllOrders();

        assertThat(actual.getOrderDataList().get(0).getId(), is(order.get(0).getId()));
        assertThat(actual.getOrderDataList().get(1).getId(), is(order.get(1).getId()));
    }

    @Test
    public void shouldFindProducts() {
        List<Order> order = newArrayList(
                new Order("o1", "1", "p1", 1),
                new Order("o2", "2", "p2", 1));
        given(orderRepository.findById("o1")).willReturn(Optional.of(order.get(0)));

        ProductList actual = orderService.findProducts("o1");

        assertThat(actual.getProducts().size(), is(1));
        assertThat(actual.getProducts().get(0).getProductId(), is("1"));
    }

    @Test(expected = OrderNotFoundException.class)
    public void shouldThrowOrderNotFound() {
        orderService.findProducts("o3");
    }

    @Test
    public void shouldCreateOrder() {
        Order order = new Order(null, "1", "p1", 1);
        Order orderWithId = new Order("1", "1", "p1", 1);
        given(orderRepository.save(order)).willReturn(orderWithId);

        String createdOrderId = orderService.createOrder(new OrderData(null, newArrayList(new OrderLineData(new Product("1", "p1"), 1))));

        assertThat(createdOrderId, is(orderWithId.getId()));
    }

    @Test
    public void shouldUpdateOrder() {
        Order orderWithId = new Order("1", "1", "p1", 1);

        orderService.updateOrder(new OrderData("1", newArrayList(new OrderLineData(new Product("1", "p1"), 1))));

        verify(orderRepository, times(1)).save(orderWithId);
    }

    @Test
    public void shouldThrowExceptionIfIdIsNullWhileUpdatingOrder() {

        try {
            orderService.updateOrder(new OrderData(null, newArrayList(new OrderLineData(new Product("1", "p1"), 1))));
            fail("Runtime exception should have been thrown");
        } catch(RuntimeException e) {
            assertThat(e.getMessage(), is("Order ID is null"));
        }

        verify(orderRepository, never()).save(any());
    }

}
