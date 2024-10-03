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
    private Float Medicine;
    private Float Education;
    private Float Travel;
    private Float Rent;
    private Float Transportation;
    private Float Utilities;
    private Float Savings;
    private Float Charity;
    private Float Insurance;
    private Float Gifts;
    private Float Recive;
    private Float Transfer;
    private Float Others;
}
