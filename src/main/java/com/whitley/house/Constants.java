package com.whitley.house;

import com.google.common.collect.ImmutableMap;

/**
 * @author yuanxin
 * @date 2022-08-27
 */
public class Constants {

    public interface City {
        public static final ImmutableMap<String, String> CITY_MAP = ImmutableMap.of(
                "北京", "bj",
                "杭州","hz");
    }

    public interface Region {
        public static final ImmutableMap<String, String> BJ_REGION_MAP = ImmutableMap.of(
                "chaoyang", "朝阳区",
                "tongzhou","通州区");

        public static final ImmutableMap<String, String> HZ_REGION_MAP = ImmutableMap.of(
                "xihu", "西湖区",
                "gongshu", "拱墅区",
                "shangcheng", "上城区",
                "yuhang", "余杭区",
                "binjiang", "滨江区");
    }
}
