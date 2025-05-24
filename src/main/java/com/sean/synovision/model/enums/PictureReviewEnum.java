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
 * @Date 2025/59/20
 */
@Getter
public enum PictureReviewEnum {

    REVIEWING("待审核",0),
    PASS("通过",1),
    REJECT("拒绝",2);


    private final String text;
    private final int value;

     PictureReviewEnum(String text, int value) {
        this.text = text;
        this.value = value;
     }

     public static final Map<Integer, PictureReviewEnum> ENU_MAP = Arrays.stream(values())
             .collect(Collectors.toMap(PictureReviewEnum::getValue, Function.identity()));

    /**
     * 获取值列表
     *
     * @return List<Integer>
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value value
     * @return PictureReviewEnum
     */
    public static PictureReviewEnum getEnumByValue(Integer value) {
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
