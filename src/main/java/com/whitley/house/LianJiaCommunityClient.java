package com.whitley.house;

import static com.whitley.house.Constants.City.CITY_MAP;
import static com.whitley.house.Constants.Region.HZ_REGION_MAP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.whitley.house.bean.Community;
import com.whitley.house.bean.House;

/**
 * 爬虫页面 https://hz.lianjia.com/xiaoqu/gongshu/
 * 通过爬取各个区域全部小区，再通过全部小区爬取房源
 * @author yuanxin
 * @date 2022/8/24
 */
public class LianJiaCommunityClient extends HttpClient {
    public static final String CITY = "杭州";
    public static final String HOME;
    static {
        String path = LianJiaCommunityClient.class.getClassLoader().getResource("").getPath();
        HOME = path.replace("target/classes/", "src/main/resources/");
    }

    public static void main(String[] args) throws Exception {
        fetchCommunity();
    }

    /**
     * 爬取小区页，https://hz.lianjia.com/xiaoqu/gongshu/
     * @throws Exception
     */
    public static void fetchCommunity() throws Exception {
        LianJiaCommunityClient lianJiaClient = new LianJiaCommunityClient();
        BufferedWriter writer = new BufferedWriter(new FileWriter(HOME + CITY_MAP.get(CITY) + "_Community.txt", true));
        String condition = "";

        for (String region : HZ_REGION_MAP.keySet()) {
            int num = 1, page = 1;
            do {
                String url = "https://"+ CITY_MAP.get(CITY) +".lianjia.com/xiaoqu/" + region +"/" + condition;
                if (page > 1) {
                    url = "https://"+ CITY_MAP.get(CITY) +".lianjia.com/xiaoqu/" + region +"/" + "pg" + page + condition;
                }
                String res = lianJiaClient.get(url);
                Document root = Jsoup.parse(res);
                Element total = root.select("body > div.content > div.leftContent > div.resultDes.clear > h2 > span").first();
                int totalNum = NumberUtils.toInt(total.text());
                num = totalNum - page * 30;
                System.out.printf("totalNum:%d, curPage:%d, num:%d%n", totalNum, page, num);
                Elements elements = root.select("body > div.content > div.leftContent > ul > li");
                for (Element element : elements) {
                    Community community = buildCommunity(element);
                    writer.write(JSON.toJSONString(community) + "\n");
                    writer.flush();
                    System.out.println(community);
                }
                page ++;
            } while (num > 0);
        }
        writer.flush();
        writer.close();
    }

    /**
     * 爬取目录页，https://bj.lianjia.com/ershoufang/chaoyang/ie2y2f2dp1sf1a2a3a4bp0ep550/
     * @throws Exception
     */
    public static void fetchMenu() throws Exception {
        LianJiaCommunityClient lianJiaClient = new LianJiaCommunityClient();
        BufferedReader reader = new BufferedReader(new FileReader(new File(HOME + CITY_MAP.get(CITY) +  "_Community.txt")));
        List<Community> communities = reader.lines().map(line -> JSON.parseObject(line, Community.class)).collect(Collectors.toList());
        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(HOME + CITY_MAP.get(CITY) + "_house.txt", true));
        String condition = "";
        for (Community community : communities) {
            int num = 1, page = 1;
            do {
                String url = community.getUrl() + condition;
                if (page > 1) {
                    url = "https://bj.lianjia.com/ershoufang/" + "pg" + page + condition;
                }
                String res = lianJiaClient.get(url);
                Document root = Jsoup.parse(res);
                Element total = root.select("#content > div.leftContent > div.resultDes.clear > h2 > span").first();
                int totalNum = NumberUtils.toInt(total.text());
                num = totalNum - page * 30;
                System.out.printf("totalNum:%d, curPage:%d, num:%d%n", totalNum, page, num);
                Elements elements = root.select("#content > div.leftContent > ul > li > div.info.clear");
                for (Element element : elements) {
                    House house = buildSimpleHouse(element);
                    writer.write(JSON.toJSONString(house) + "\n");
                    writer.flush();
                    System.out.println(house);
                }
                page ++;
            } while (num > 0);
        }
        writer.flush();
        writer.close();
    }

    public static void fetchDetail() throws Exception {
        LianJiaCommunityClient lianJiaClient = new LianJiaCommunityClient();
        BufferedReader reader = new BufferedReader(new FileReader(new File(HOME + CITY_MAP.get(CITY) +  "_house.txt")));
        List<House> houses = reader.lines().map(line -> JSON.parseObject(line, House.class)).collect(Collectors.toList());
        reader.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(HOME + CITY_MAP.get(CITY) +  "_house_detail.txt", true));

        for (House house : houses) {
            String res = lianJiaClient.get(house.getUrl());
            buildDetail(Jsoup.parse(res), house);
            writer.write(JSON.toJSONString(house) + "\n");
            writer.flush();
            System.out.println(house);
        }

        writer.flush();
        writer.close();
    }

    private static Community buildCommunity(Element li) {
        Element element = li.selectFirst("div.xiaoquListItemRight > div.xiaoquListItemSellCount > a");
        return new Community(element.attributes().get("title"), element.attributes().get("href"));
    }

    private static House buildSimpleHouse(Element element) {
        Element title = element.select("div.title > a").first();
        House house = new House();
        house.setHouseCode(title.attributes().get("data-housecode"));
        house.setUrl(title.attributes().get("href"));
        house.setCity(CITY);
        house.setTitle(title.text());

        Element price = element.select("div.priceInfo > div.totalPrice > span").first();
        Element unitPrice = element.select("div.priceInfo > div.unitPrice > span").first();
        String unitPriceInfo = unitPrice.text();
        unitPriceInfo = unitPriceInfo.substring(0, unitPriceInfo.length() - 3);
        unitPriceInfo = unitPriceInfo.replace(",", "");
        house.setPrice(NumberUtils.toInt(price.text()));
        house.setUnitPrice(NumberUtils.toInt(unitPriceInfo));

        Elements positions = element.select("div.flood > div.positionInfo > a");
        house.setCommunity(positions.first().text());
        house.setArea(positions.last().text());

        Element houseInfo = element.select("div.address > div.houseInfo").first();
        house.setHouseInfo(houseInfo.text());
        return house;
    }

    private static void buildDetail(Document root, House house) {
        Elements area = root.select("body > div.overview > div.content > div.aroundInfo > div.areaName > span.info");
        house.setAreaDetail(area.text());
        house.setRegion(area.text().split(" ")[0]);
        // 首付
        Element calculator = root.getElementById("calculator");
        JSONObject jsonObject = JSON.parseObject(calculator.attributes().get("data-shoufu"));
        if (jsonObject == null) {
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
        }

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
