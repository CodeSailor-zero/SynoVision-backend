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
 * @Date 2025/06/2
 * 空间类型枚举
 */
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私人空间", 0),
    TEAM("团队空间", 1);

    private final String text;
    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static final Map<Integer, SpaceTypeEnum> ENU_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(SpaceTypeEnum::getValue, Function.identity()));

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
    public static SpaceTypeEnum getEnumByValue(Integer value) {
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
