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
 * 空间角色类型枚举
 */
@Getter
public enum SpaceRoleEnum {

    VIEWER("浏览者", "viewer"),
    EDITOR("编辑者", "editor"),
    ADMIN("管理者", "admin");

    private final String text;
    private final String value;

    SpaceRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static final Map<String, SpaceRoleEnum> ENU_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(SpaceRoleEnum::getValue, Function.identity()));

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
    public static SpaceRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        return ENU_MAP.get(value);
    }
}
