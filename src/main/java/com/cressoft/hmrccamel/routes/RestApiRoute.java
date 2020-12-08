package com.cressoft.hmrccamel.routes;

import com.cressoft.hmrccamel.data.OrderData;
import com.cressoft.hmrccamel.data.OrderDataList;
import com.cressoft.hmrccamel.data.ProductList;
import com.cressoft.hmrccamel.exception.OrderNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

@Component
public class RestApiRoute extends RouteBuilder {

    private static Logger logger = LoggerFactory.getLogger(RestApiRoute.class);

    @Autowired
    private Environment env;

    @Value("${server.port}")
    private String serverPort;


    @Value("${context.path}")
    private String contextPath;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .contextPath(contextPath)
                .port(serverPort)
                .enableCORS(true)
                .apiContextPath("/doc");

        rest("/orders").produces("application/json").consumes("application/json")
                .get()
                .to("direct:orders-get")
                .outType(OrderDataList.class)

                .get("/{id}/products")
                .param().name("id").type(RestParamType.path).dataType("string").endParam()
                .to("direct:order-products-get")
                .outType(ProductList.class);

        rest("/orders").produces("application/json").consumes("application/json")
                .post()
                .route()
                .to("direct:fe-validate-order-json")
                .to("direct:order-create");

        rest("/orders").produces("application/json").consumes("application/json")
                .put("/{id}")
                .route()
                .to("direct:fe-validate-order-json")
                .to("direct:order-update");

        onException(ValidationException.class)
                .handled(true)
                .setHeader(HTTP_RESPONSE_CODE, constant(400))
                .log("occurredAt: ${date:now} ,\nmessage: ${exception.message},\nexception: ${exception.stacktrace} ")
                .setBody(simple("Validation Error occurred for message with id= ${id}"));

        onException(OrderNotFoundException.class)
                .handled(true)
                .setHeader(HTTP_RESPONSE_CODE, constant(404))
                .log("occurredAt: ${date:now} ,\nmessage: ${exception.message},\nexception: ${exception.stacktrace} ")
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    logger.error(exception.getMessage());
                    exchange.getIn().setBody("Order can't be found");
                });

        onException(RuntimeException.class)
                .handled(true)
                .setHeader(HTTP_RESPONSE_CODE, constant(500))
                .log("occurredAt: ${date:now} ,\nmessage: ${exception.message},\nexception: ${exception.stacktrace} ")
                .setBody(simple("Internal Server Error occurred for message with id= ${id}"));

        from("direct:fe-validate-order-json")
                .log("validation of order input has started")
                .marshal().json(JsonLibrary.Jackson, true)
                .to("json-validator:fe-order-schema.json")
                .log("validation of order input is successful");


    }
}
