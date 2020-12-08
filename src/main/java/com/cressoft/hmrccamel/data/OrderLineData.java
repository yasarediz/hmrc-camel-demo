package com.cressoft.hmrccamel.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineData {
    private Product product;
    private Integer quantity;
}
