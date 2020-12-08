package com.cressoft.hmrccamel.service;

import com.cressoft.hmrccamel.data.*;
import com.cressoft.hmrccamel.exception.OrderNotFoundException;
import com.cressoft.hmrccamel.model.Order;
import com.cressoft.hmrccamel.model.OrderLine;
import com.cressoft.hmrccamel.repo.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public OrderDataList findAllOrders() {
        return new OrderDataList(
            newArrayList(orderRepository.findAll())
                .stream()
                .map(order ->
                        new OrderData(order.getId(), newArrayList(new OrderLineData(new Product(order.getProductId(), order.getProductName()), order.getQuantity())))
                )
                .collect(toList())
        );
    }

    public ProductList findProducts(String orderId) {
        List<Product> products = orderRepository.findById(orderId)
                .map(order -> newArrayList(new Product(order.getProductId(), order.getProductName())))
                .orElseThrow(OrderNotFoundException::new);
        return new ProductList(products);
    }

    public String createOrder(OrderData orderData) {
        Order orderToBeSave = new Order();
        orderData.getOrderLines().stream()
                .forEach(orderLineData -> {
                    orderToBeSave.setProductId(orderLineData.getProduct().getProductId());
                    orderToBeSave.setProductName(orderLineData.getProduct().getProductName());
                    orderToBeSave.setQuantity(orderLineData.getQuantity());
                });
        Order order = orderRepository.save(orderToBeSave);
        return order.getId();
    }

    public void updateOrder(OrderData orderData) {
        if(orderData.getId() == null) throw new RuntimeException("Order ID is null");

        // update record
        Order orderToBeSave = new Order();
        orderToBeSave.setId(orderData.getId());
        orderData.getOrderLines().stream()
                .forEach(orderLineData -> {
                    orderToBeSave.setProductId(orderLineData.getProduct().getProductId());
                    orderToBeSave.setProductName(orderLineData.getProduct().getProductName());
                    orderToBeSave.setQuantity(orderLineData.getQuantity());
                });
        orderRepository.save(orderToBeSave);
    }
}
