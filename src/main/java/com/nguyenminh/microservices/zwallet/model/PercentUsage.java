package com.nguyenminh.microservices.zwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PercentUsage {
    private Float Food;
    private Float Bill;
    private Float Entertain;
    private Float Shopping;
    private Float Investment;
}
