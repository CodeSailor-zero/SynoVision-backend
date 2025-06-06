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
public enum UserRoleEnum {

    USER("用户","user"),
    ADMIN("管理员","admin");
    private final String text;
    private final String value;

     UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
     }

     public static final Map<String,UserRoleEnum> ENU_MAP = Arrays.stream(values())
             .collect(Collectors.toMap(UserRoleEnum::getValue, Function.identity()));

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static UserRoleEnum getEnumByValue(String value) {
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
