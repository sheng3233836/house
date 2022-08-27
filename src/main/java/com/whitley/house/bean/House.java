package com.whitley.house.bean;

import java.util.Map;

import lombok.Data;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.ImmutableMap;

/**
 * @author yuanxin
 * @date 2022/8/24
 */
@Data
public class House {
    private String houseCode;
    private String url;
    private String city;
    private String region;

    private String title;
    /** 小区 */
    private String community;
    /** 区域 */
    private String area;
    /** 总价 单位万 */
    private int price;
    /** 单位价格 */
    private int unitPrice;
    private String houseInfo;
    /** 房型 x室x厅 */
    private String houseType;
    /** 建筑面积 */
    private double square;
    /** 朝向 */
    private String orientation;
    /** 装修风格，简装、精装 */
    private String decorate;
    /** 户型结构 平层、复式 */
    private String structure;
    /** 楼层信息 */
    private String floorInfo;
    /** 建成时间 */
    private int buildYear;
    /** 楼类型，板楼、塔楼 */
    private String buildType;

    private String areaDetail;
    private double realSquare;

    private double firstPayFee;
    private double purePayFee;
    private double taxFee;
    private Map<String, Double> taxDetail;

    /** 等额本息*/
    private int annuity;
    /** 等额本金*/
    private int linear;
    /** 等额本金-每月递减*/
    private double linearCut;

    /** 基本属性*/
    private Map<String, String> baseAttr;
    /** 交易属性*/
    private Map<String, String> saleAttr;
    /** 核心卖点 */
    private String corePoint;
    /** 交通出行 */
    private String transport;

    public void setHouseInfo(String houseInfo) {
        this.houseInfo = houseInfo;
        String[] split = houseInfo.split("\\|");
        if (split.length == 7) {
            this.setHouseType(split[0].trim());
            this.setSquare(NumberUtils.toDouble(split[1].trim().substring(0, split[1].trim().length() - 2)));
            this.setOrientation(split[2].trim());
            this.setDecorate(split[3].trim());
            this.setFloorInfo(split[4].trim());
            this.setBuildType(split[6].trim());
            if (split[5].contains("年建")) {
                this.setBuildYear(NumberUtils.toInt(split[5].trim().substring(0, 4)));
            } else {
                System.out.println(this.getHouseCode() + " buildYear ERROR:" + split[5] + ", houseCode");
            }
        } else if (split.length == 6 && !houseInfo.contains("年建")) {
            this.setHouseType(split[0].trim());
            this.setSquare(NumberUtils.toDouble(split[1].trim().substring(0, split[1].trim().length() - 2)));
            this.setOrientation(split[2].trim());
            this.setDecorate(split[3].trim());
            this.setFloorInfo(split[4].trim());
            this.setBuildType(split[5].trim());
        } else {
            System.out.println(this.getHouseCode() + " houseInfo ERROR:" + houseInfo);
        }
    }
}
