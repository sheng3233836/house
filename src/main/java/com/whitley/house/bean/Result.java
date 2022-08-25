package com.whitley.house.bean;

import java.util.List;

import lombok.Data;

/**
 * @author yuanxin
 * @date 2022/8/25
 */
@Data
public class Result {
    private String community;
    private int size;
    private int unitPrice;
    private List<HouseResult> houses;

    @Data
    public static class HouseResult {
        private String houseCode;
        private String url;
        private String title;
        private int price;
        private int unitPrice;
        private double firstPayFee;
        private int annuity;
        private String houseInfo;
    }
}
