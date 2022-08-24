package com.whitley.house;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.whitley.house.bean.House;

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

        compute(houses);
    }

    private static void compute(List<House> houses) {


    }
}
