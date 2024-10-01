package com.nguyenminh.microservices.zwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PercentUsageTotal {
    private BigDecimal Food;
    private BigDecimal Bill;
    private BigDecimal Entertain;
    private BigDecimal Shopping;
    private BigDecimal Investment;
}
