package com.sean.synovision.job;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sean
 * @Date 2025/33/24
 */
public class test {
    public static Map<Object, Integer> countElementOccurrences(Object[][] arrays) {
        Map<Object, Integer> counts = new HashMap<>();

        for (Object[] arr : arrays) {
            for (Object element : arr) {
                counts.put(element, counts.getOrDefault(element, 0) + 1);
            }
        }

        return counts;
    }

    public static void main(String[] args) {
        // 示例数据
        String[][] a = {
                {"热门"},
                {"热门"},
                {"热门","搞笑"},
                {"校园"},
                {"艺术"}
        };
//        int[][] arrays = {
//                {1, 2, 3, 2},
//                {2, 3, 4, 5},
//                {5, 6, 2, 1}
//        };

        // 统计元素出现次数
        Map<Object, Integer> result = countElementOccurrences(a);

        // 打印结果
        System.out.println("元素出现次数统计：");
        for (Map.Entry<Object, Integer> entry : result.entrySet()) {
            System.out.println("元素 " + entry.getKey() + " 出现次数: " + entry.getValue());
        }
    }
}
