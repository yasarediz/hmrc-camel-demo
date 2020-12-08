package com.cressoft.hmrccamel.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {
    private String id; // Optional
    private List<OrderLineData> orderLines;
}
