package com.whitley.house;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.whitley.house.bean.House;

/**
 * 爬虫页面 https://map.lianjia.com/map/110000/ESF
 * 通过地图全域爬取
 * @author yuanxin
 * @date 2022/8/24
 */
public class LianJiaMapClient extends HttpClient {
    public static final String CITY = "北京";
    public static final String REGION = "tongzhou";
    public static final String HOME;
    static {
        String path = LianJiaMapClient.class.getClassLoader().getResource("").getPath();
        HOME = path.replace("target/classes/", "src/main/resources/");
    }

    public static void main(String[] args) throws Exception {
        Document parse = Jsoup.parse(new File(HOME + "tmp_fang"), "UTF-8");
        System.out.println(parse);
    }

    private static House buildSimple(Element root) {
        Element title = root.select("div.title > a").first();
        House house = new House();
        house.setHouseCode(title.attributes().get("data-housecode"));
        house.setUrl(title.attributes().get("href"));
        house.setCity(CITY);
        house.setRegion(REGION);
        house.setTitle(title.text());
        return house;
    }

    private static void buildDetail(Element root, House house) {
        // 房价
        Element price = root.select("body > div.overview > div.content > div.price-container > div > span.total").first();
        Element unitPrice = root.select("body > div.overview > div.content > div.price-container > div > div.text > div.unitPrice > span").first();
        house.setPrice(NumberUtils.toInt(price.text()));
        house.setUnitPrice(NumberUtils.toInt(unitPrice.childNode(0).toString()));
        // 房屋区域
        Element community = root.selectFirst("body > div.overview > div.content > div.aroundInfo > div.communityName > a.info");
        house.setCommunity(community.text());
        Elements area = root.select("body > div.overview > div.content > div.aroundInfo > div.areaName > span.info");
        house.setArea(area.text().split(" ")[1]);
        house.setAreaDetail(area.text());

        // 房型信息
        Element houseInfo = root.select("body > div.overview > div.content > div.houseInfo").first();
        house.setHouseType(houseInfo.selectFirst("div.room > div.mainInfo").text());
        house.setFloorInfo(houseInfo.selectFirst("div.room > div.subInfo").text());
        house.setOrientation(houseInfo.selectFirst("div.type > div.mainInfo").text());
        String[] decorateSplit = houseInfo.selectFirst("div.type > div.subInfo").text().split("/");
        house.setStructure(decorateSplit[0]);
        house.setDecorate(decorateSplit[1]);
        String areaSquare = houseInfo.selectFirst("div.area > div.mainInfo").text();
        house.setSquare(NumberUtils.toDouble(areaSquare.trim().substring(0, areaSquare.trim().length() - 2)));
        String[] buildSplit = houseInfo.selectFirst("div.area > div.subInfo").text().split("/");
        house.setBuildType(buildSplit[6].trim());
        if (buildSplit[0].contains("年建")) {
            house.setBuildYear(NumberUtils.toInt(buildSplit[0].trim().substring(0, 4)));
        }

        // 首付
        Element calculator = root.getElementById("calculator");
        JSONObject jsonObject = JSON.parseObject(calculator.attributes().get("data-shoufu"));
        house.setFirstPayFee(jsonObject.getDoubleValue("totalShoufu"));
        house.setPurePayFee(jsonObject.getDoubleValue("pureShoufu"));
        JSONObject taxResult = jsonObject.getJSONObject("taxResult");
        if (taxResult != null) {
            house.setTaxFee(taxResult.getDoubleValue("taxTotal"));
            house.setTaxDetail(new HashMap<>());
            JSONArray taxFees = taxResult.getJSONArray("taxFees");
            for (int i = 0; i < taxFees.size(); i++) {
                JSONObject taxFee = taxFees.getJSONObject(i);
                house.getTaxDetail().put(taxFee.getString("name"), taxFee.getDoubleValue("value"));
            }
        }
        // 月供
        house.setAnnuity(jsonObject.getBigDecimal("monthPayWithInterest").setScale(0, RoundingMode.UP).intValue());
        house.setLinear(jsonObject.getBigDecimal("monthPay").setScale(0, RoundingMode.UP).intValue());
        house.setLinearCut(jsonObject.getBigDecimal("monthReduce").setScale(2, RoundingMode.UP).doubleValue());

        // 额外信息
        Elements base = root.select("#introduction > div > div > div.base > div.content > ul > li");
        house.setBaseAttr(new HashMap<>());
        for (Element element : base) {
            house.getBaseAttr().put(element.child(0).text(), element.childNode(1).toString());
        }
        Elements sale = root.select("#introduction > div > div > div.transaction > div.content > ul > li");
        house.setSaleAttr(new HashMap<>());
        for (Element element : sale) {
            house.getSaleAttr().put(element.child(0).text(), element.child(1).text());
        }
        Elements more = root.select("body > div.m-content > div.box-l > div:nth-child(2) > div.introContent.showbasemore > div.baseattribute.clear");
        for (Element element : more) {
            if (element.text().contains("核心卖点")) {
                house.setCorePoint(element.text());
            } else if (element.text().contains("交通出行")) {
                house.setTransport(element.text());
            }
        }
        String real = house.getBaseAttr().get("套内面积");
        house.setRealSquare(NumberUtils.toDouble(real.substring(0, real.length() - 1)));
    }

    @Override
    public void setHeader(HttpRequestBase requestBase) {
    }

}
