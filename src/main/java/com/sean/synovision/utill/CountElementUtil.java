package com.sean.synovision.utill;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sean
 * @Date 2025/47/24
 * 计算 tags的出现次数，进行动态推荐
 */
public class CountElementUtil {
    public static Map<Object, Integer> countElementOccurrences(Object[][] arrays) {
        Map<Object, Integer> counts = new HashMap<>();

        for (Object[] arr : arrays) {
            for (Object element : arr) {
                counts.put(element, counts.getOrDefault(element, 0) + 1);
            }
        }

        return counts;
    }
}
