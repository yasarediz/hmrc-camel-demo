package com.cressoft.hmrccamel;

import com.cressoft.hmrccamel.data.*;
import com.cressoft.hmrccamel.model.Order;
import com.cressoft.hmrccamel.model.OrderLine;
import com.cressoft.hmrccamel.repo.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ContextConfiguration(classes = HmrcCamelApplication.class, loader = SpringBootContextLoader.class)
public class HmrcCamelApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private OrderRepository repository;

	@Before
	public void setup() {
		repository.deleteAll();
	}

	@Test
	public void shouldFindAllOrders() throws Exception {
		// Given
		Order order1 = new Order(null, "1", "product1", 2);
		Order order2 = new Order(null, "2", "product2", 2);

		// create orders in db
		repository.save(order1);
		repository.save(order2);

		// When
		ResponseEntity<OrderDataList> actual = restTemplate.getForEntity("/api/orders", OrderDataList.class);

		// Then
		assertThat(actual.getStatusCode(), is(OK));
//		ObjectMapper mapper = new ObjectMapper();
//		OrderDataList actualOrders = mapper.readValue(actual.getBody(), OrderDataList.class);
		assertThat(actual.getBody().getOrderDataList().size(), is(2));
		String productName = actual.getBody().getOrderDataList().get(0).getOrderLines().get(0).getProduct().getProductName();
		assertTrue(productName.equals("product1") || productName.equals("product2"));
		productName = actual.getBody().getOrderDataList().get(1).getOrderLines().get(0).getProduct().getProductName();
		assertTrue(productName.equals("product1") || productName.equals("product2"));
	}

	@Test
	public void shouldFindProductsOfAnOrder() throws Exception {
		// Given
		Order order1 = new Order("1", "1", "product1", 2);
		Order order2 = new Order("2", "2", "product2", 2);

		// create orders in db
		repository.save(order1);
		repository.save(order2);

		Order targetedOrder = newArrayList(repository.findAll()).get(0);
		String orderId = targetedOrder.getId();

		// When
		ResponseEntity<ProductList> actual = restTemplate.getForEntity(format("/api/orders/%s/products", orderId), ProductList.class);

		// Then
		assertThat(actual.getStatusCode(), is(OK));
		assertThat(actual.getBody().getProducts().size(), is(1));
		assertThat(actual.getBody().getProducts().get(0).getProductId(), is(targetedOrder.getProductId()));
	}

	@Test
	public void shouldCreateOrder() throws Exception {
		// Given
		OrderData orderData = new OrderData(null,
				newArrayList(new OrderLineData(new Product("1", "product1"), 2)));

		// When
		ResponseEntity<String> actual = restTemplate.postForEntity("/api/orders", orderData, String.class);

		// Then
		assertThat(actual.getStatusCode(), is(CREATED));
		Optional<Order> order = repository.findById(actual.getBody().substring(1, actual.getBody().length()-1));
		assertTrue(order.isPresent());
		assertThat(order.get().getProductId(), is("1"));
	}

	@Test
	public void shouldUpdateOrder() throws Exception{
		// Given
		Order order1 = new Order(null, "1", "product1", 2);
		// create orders in db
		repository.save(order1);
		order1 = newArrayList(repository.findAll()).get(0);

		// When
		OrderData orderData = new OrderData(order1.getId(),
				newArrayList(new OrderLineData(new Product("1", "product1"), 4)));
		ResponseEntity<String> actual = restTemplate.exchange(format("/api/orders/%s", order1.getId()), HttpMethod.PUT, new HttpEntity<>(orderData), String.class);

		// Then
		assertThat(actual.getStatusCode().value(), is(204));
		assertThat(newArrayList(repository.findAll()).get(0).getQuantity(), is(4));

	}

	@Test
	public void shouldCatchExceptionWhenNoBody() {
		// Given there is no order

		// When
		ResponseEntity<String> actual = restTemplate.postForEntity("/api/orders", null, String.class);

		// Then
		assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
	}
}
