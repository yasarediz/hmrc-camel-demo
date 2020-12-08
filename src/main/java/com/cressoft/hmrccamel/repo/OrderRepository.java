package com.cressoft.hmrccamel.repo;

import com.cressoft.hmrccamel.model.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, String> {
}
