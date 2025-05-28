package com.sean.synovision.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sean
 * @Date 2025/05/26
 */
@Getter
public enum SpaceLevelEnum {

    COMMON("普通版",0,100,100L * 1024 * 1024),
    PROFESSIONAL("专业版",1,1000,1000L * 1024 * 1024),
    FLAGSHIP("旗舰版",2,10000,10000L * 1024 * 1024);
    private final String text;
    private final int value;
    private final long maxCount;
    private final long maxSize;

     SpaceLevelEnum(String text, int value,long maxCount,long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
     }

     public static final Map<Integer, SpaceLevelEnum> ENU_MAP = Arrays.stream(values())
             .collect(Collectors.toMap(SpaceLevelEnum::getValue, Function.identity()));

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
//        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
////            if (anEnum.value.equals(value)) {
////                return anEnum;
////            }
////        }
        return ENU_MAP.get(value);
    }
}
