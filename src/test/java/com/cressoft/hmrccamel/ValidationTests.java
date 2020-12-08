package com.cressoft.hmrccamel;

import com.cressoft.hmrccamel.data.*;
import com.cressoft.hmrccamel.model.Order;
import com.cressoft.hmrccamel.repo.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ValidationException;
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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ContextConfiguration(classes = HmrcCamelApplication.class, loader = SpringBootContextLoader.class)
public class ValidationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void shouldFailValidationWhenCreatingOrUpdatingOrder() {
		// Given
		OrderData orderData = new OrderData("1",
				newArrayList(new OrderLineData(new Product(null, "product1"), 2)));
		// Product id can't be null, validation error!!

		// When
		ResponseEntity<String> actual = restTemplate.postForEntity("/api/orders", orderData, String.class);

		// Then
		assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertTrue(actual.getBody().contains("Validation Error occurred for message"));

		// When
		actual = restTemplate.exchange(format("/api/orders/%s", orderData.getId()), HttpMethod.PUT, new HttpEntity<>(orderData), String.class);

		// Then
		assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertTrue(actual.getBody().contains("Validation Error occurred for message"));
	}
}
