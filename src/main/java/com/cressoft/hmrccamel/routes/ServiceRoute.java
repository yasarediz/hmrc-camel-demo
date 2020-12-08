package com.cressoft.hmrccamel.routes;

import com.cressoft.hmrccamel.data.OrderData;
import com.cressoft.hmrccamel.data.OrderDataList;
import com.cressoft.hmrccamel.data.ProductList;
import com.cressoft.hmrccamel.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceRoute extends RouteBuilder {

    @Autowired
    private OrderService orderService;

    @Override
    public void configure() throws Exception {

        from("direct:orders-get")
                .tracing()
                .log("Message body is ${body}")
                .to("bean:orderService?method=findAllOrders")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .outputType(OrderDataList.class);

        from("direct:order-products-get")
                .tracing()
                .log("Message body is ${body}")
                .to("bean:orderService?method=findProducts(${header.id})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .outputType(ProductList.class);

        from("direct:order-create")
                .tracing()
                .log("Message body is ${body}")
                .process(exchange -> {
                    String orderData = exchange.getIn().getBody(String.class);
                    String orderId = orderService.createOrder(new ObjectMapper().readValue(orderData, OrderData.class));
                    exchange.getIn().setBody(orderId);
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .outputType(String.class);

        from("direct:order-update")
                .tracing()
                .log("Message body is ${body}")
                .process(exchange -> {
                    String orderData = exchange.getIn().getBody(String.class);
                    orderService.updateOrder(new ObjectMapper().readValue(orderData, OrderData.class));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .setBody(simple(null));

    }
}
