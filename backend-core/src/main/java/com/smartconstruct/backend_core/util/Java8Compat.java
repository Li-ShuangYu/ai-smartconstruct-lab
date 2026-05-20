package com.smartconstruct.backend_core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java 8 兼容性工具类
 * 
 * 提供 Java 9+ 特性的替代实现，使代码能够在 Java 8 环境下编译运行。
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
public final class Java8Compat {

    private Java8Compat() {
        // 私有构造函数，防止实例化
    }

    /**
     * 判断字符串是否为空或空白
     * 替代 Java 11+ 的 String.isBlank()
     * 
     * @param str 字符串
     * @return 如果字符串为 null、空或仅包含空白字符，返回 true
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空且非空白
     * 
     * @param str 字符串
     * @return 如果字符串不为 null 且包含非空白字符，返回 true
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 创建一个空的 ArrayList
     * 替代 Java 9+ 的 List.of()
     * 
     * @param <T> 元素类型
     * @return 空的 ArrayList
     */
    public static <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    /**
     * 创建包含指定元素的 ArrayList
     * 替代 Java 9+ 的 List.of(e1, e2, ...)
     * 
     * @param elements 元素
     * @param <T>      元素类型
     * @return 包含指定元素的 ArrayList
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        List<T> list = new ArrayList<>();
        if (elements != null) {
            Collections.addAll(list, elements);
        }
        return list;
    }

    /**
     * 创建一个空的 HashMap
     * 替代 Java 9+ 的 Map.of()
     * 
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 空的 HashMap
     */
    public static <K, V> Map<K, V> emptyMap() {
        return new HashMap<>();
    }

    /**
     * 创建包含一个键值对的 HashMap
     * 替代 Java 9+ 的 Map.of(k1, v1)
     * 
     * @param k1 键
     * @param v1 值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    /**
     * 创建包含两个键值对的 HashMap
     * 替代 Java 9+ 的 Map.of(k1, v1, k2, v2)
     * 
     * @param k1 键1
     * @param v1 值1
     * @param k2 键2
     * @param v2 值2
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * 创建包含三个键值对的 HashMap
     * 替代 Java 9+ 的 Map.of(k1, v1, k2, v2, k3, v3)
     * 
     * @param k1 键1
     * @param v1 值1
     * @param k2 键2
     * @param v2 值2
     * @param k3 键3
     * @param v3 值3
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    /**
     * 创建包含四个键值对的 HashMap
     * 替代 Java 9+ 的 Map.of(k1, v1, k2, v2, k3, v3, k4, v4)
     * 
     * @param k1 键1
     * @param v1 值1
     * @param k2 键2
     * @param v2 值2
     * @param k3 键3
     * @param v3 值3
     * @param k4 键4
     * @param v4 值4
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    /**
     * 创建包含五个键值对的 HashMap
     * 替代 Java 9+ 的 Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5)
     * 
     * @param k1 键1
     * @param v1 值1
     * @param k2 键2
     * @param v2 值2
     * @param k3 键3
     * @param v3 值3
     * @param k4 键4
     * @param v4 值4
     * @param k5 键5
     * @param v5 值5
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }
}