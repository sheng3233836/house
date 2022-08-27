package com.whitley.house;

import static com.whitley.house.Constants.Region.BJ_REGION_MAP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.whitley.house.bean.House;
import com.whitley.house.bean.Result;

/**
 * @author yuanxin
 * @date 2022-08-24
 */
public class Compute {

    public static final String HOME;

    static {
        String path = Compute.class.getClassLoader().getResource("").getPath();
        HOME = path.replace("target/classes/", "src/main/resources/");
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(HOME + "house_detail.txt")));
        List<House> houses = reader.lines().map(line -> JSON.parseObject(line, House.class)).collect(Collectors.toList());
        reader.close();

        printResult(compute(houses));
    }

    private static List<House> compute(List<House> houses) {
        List<House> res = houses.stream()
                .filter(house -> house.getFirstPayFee() < 1800000 && house.getTaxDetail().size() == 1 && house.getTaxDetail()
                        .containsKey("契税") && house.getSquare() > 65)
                .collect(Collectors.toList());
        System.out.println(res.size());
        return res;
    }

    private static void printResult(List<House> houses) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(HOME + "result.json"));
        Map<String, List<House>> communityMap = houses.stream().collect(Collectors.groupingBy(house -> BJ_REGION_MAP.get(house.getRegion()) + house.getCommunity()));
        List<Result> results = new ArrayList<>();
        for (Map.Entry<String, List<House>> entry : communityMap.entrySet()) {
            Result result = new Result();
            result.setCommunity(entry.getKey());
            result.setSize(entry.getValue().size());
            result.setUnitPrice((int) entry.getValue().stream().mapToInt(House::getUnitPrice).average().getAsDouble());
            result.setHouses(entry.getValue().stream().map(Compute::toResult).collect(Collectors.toList()));
            results.add(result);
        }

        writer.write(JSON.toJSONString(results));
        writer.flush();
        writer.close();
    }

    private static Result.HouseResult toResult(House house) {
        Result.HouseResult result = new Result.HouseResult();
        result.setHouseCode(house.getHouseCode());
        result.setAnnuity(house.getAnnuity());
        result.setPrice(house.getPrice());
        result.setHouseInfo(house.getHouseInfo());
        result.setTitle(house.getTitle());
        result.setUrl(house.getUrl());
        result.setUnitPrice(house.getUnitPrice());
        result.setFirstPayFee(BigDecimal.valueOf(house.getFirstPayFee() / 10000.0).setScale(1, RoundingMode.UP).doubleValue());
        return result;
    }
}
