package com.sean.synovision.manager.websocket.model;

import com.sean.synovision.model.enums.UserRoleEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 图片编辑动作枚举
 */
@Getter
public enum PictureEditActionEnum {

    ZOOM_IN("放大操作", "ZOOM_IN"),
    ZOOM_OUT("缩小操作", "ZOOM_OUT"),
    ROTATE_LEFT("左旋操作", "ROTATE_LEFT"),
    ROTATE_RIGHT("右旋操作", "ROTATE_RIGHT");

    private final String text;
    private final String value;

    PictureEditActionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static final Map<String, PictureEditActionEnum> ENU_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(PictureEditActionEnum::getValue, Function.identity()));

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditActionEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
       return ENU_MAP.get(value);
    }
}