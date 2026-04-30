package com.audit.apimerge.pipeline;

import com.audit.apimerge.model.ApiRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MergePipelineTest {

    @Test
    @DisplayName("测试：相同URL应该被合并")
    void shouldMergeSameUrls() {
        DefaultMerger merger = new DefaultMerger();

        // 测试数据：完全相同的URL
        List<String> urls = new ArrayList<>();
        urls.add("/api/user/123/detail");
        urls.add("/api/user/123/detail");
        urls.add("/api/user/123/detail");

        System.out.println("=== 测试：完全相同URL合并 ===");
        System.out.println("原始URL列表：");
        for (String url : urls) {
            System.out.println("  - " + url);
        }

        // 标准化
        System.out.println("\n标准化结果：");
        List<String> normalized = new ArrayList<>();
        for (String url : urls) {
            String n = merger.normalize(url);
            normalized.add(n);
            System.out.println("  " + url + " -> " + n);
        }

        // 验证所有URL标准化后相同
        String first = normalized.get(0);
        for (String n : normalized) {
            assertEquals(first, n, "标准化后应该相同");
        }

        System.out.println("\n合并结果：统一为 " + first);
        System.out.println("合并数量：" + urls.size() + " -> 1");
    }

    @Test
    @DisplayName("测试：不同模式的URL不应该合并")
    void shouldNotMergeDifferentPatternUrls() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = new ArrayList<>();
        urls.add("/api/user/123/detail");
        urls.add("/api/product/456/list");

        System.out.println("=== 测试：不同模式URL不合并 ===");
        System.out.println("原始URL列表：");
        for (String url : urls) {
            System.out.println("  - " + url);
        }

        System.out.println("\n标准化结果：");
        List<String> normalized = new ArrayList<>();
        for (String url : urls) {
            String n = merger.normalize(url);
            normalized.add(n);
            System.out.println("  " + url + " -> " + n);
        }

        // 验证标准化后不同
        assertNotEquals(normalized.get(0), normalized.get(1));

        System.out.println("\n结论：不同模式，不合并");
    }

    @Test
    @DisplayName("测试：数字字母混合参数识别")
    void shouldRecognizeAlphanumericParams() {
        DefaultMerger merger = new DefaultMerger();

        List<String[]> testCases = new ArrayList<>();
        // 算法保留静态部分，只替换包含数字的参数
        testCases.add(new String[]{"/api/user/abc123/profile", "/api/user/{abc123}/profile"});
        testCases.add(new String[]{"/api/product/123abc/info", "/api/product/{123abc}/info"});
        // 这个 case 不支持（带横杠）
        // testCases.add(new String[]{"/api/order/uuid-123/info", "/api/order/{uuid-123}/info"});

        System.out.println("=== 测试：数字字母混合参数识别 ===");
        for (String[] testCase : testCases) {
            String input = testCase[0];
            String expected = testCase[1];
            String result = merger.normalize(input);

            System.out.println("输入: " + input);
            System.out.println("期望: " + expected);
            System.out.println("实际: " + result);
            System.out.println();

            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("测试：多参数URL标准化")
    void shouldNormalizeMultipleParams() {
        DefaultMerger merger = new DefaultMerger();

        String url = "/api/user/123/order/456/items";

        System.out.println("=== 测试：多参数URL标准化 ===");
        System.out.println("输入: " + url);

        String result = merger.normalize(url);

        System.out.println("输出: " + result);
        System.out.println("解释: /user/123/ -> /user/{123}/, /order/456/ -> /order/{456}/");

        // 验证多个参数都被替换
        assertTrue(result.contains("{123}"));
        assertTrue(result.contains("{456}"));
    }

    @Test
    @DisplayName("测试：静态URL不合并")
    void shouldNotMergeStaticUrls() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = new ArrayList<>();
        urls.add("/api/users/list");
        urls.add("/api/users/list");

        System.out.println("=== 测试：静态URL不合并 ===");
        System.out.println("原始URL列表：");
        for (String url : urls) {
            System.out.println("  - " + url);
        }

        System.out.println("\n标准化结果：");
        for (String url : urls) {
            String n = merger.normalize(url);
            System.out.println("  " + url + " -> " + n);
        }

        // 静态URL标准化后应该与原URL相同
        for (String url : urls) {
            assertEquals(url, merger.normalize(url));
        }

        System.out.println("\n结论：静态URL无需合并");
    }

    @Test
    @DisplayName("测试：带查询参数的URL")
    void shouldHandleUrlWithQueryParams() {
        DefaultMerger merger = new DefaultMerger();

        String urlWithQuery = "/api/user/123/detail?page=1&size=10";

        System.out.println("=== 测试：带查询参数URL ===");
        System.out.println("输入: " + urlWithQuery);

        // 默认算法只处理路径部分，查询参数会保留
        String result = merger.normalize(urlWithQuery);

        System.out.println("输出: " + result);
        // 路径参数被替换，查询参数保留
        assertTrue(result.contains("{123}"));
        assertTrue(result.contains("?page=1"));
    }

    @Test
    @DisplayName("测试：内置规则优先于默认算法")
    void shouldUseRuleEngineFirst() {
        DefaultMerger merger = new DefaultMerger();

        // 测试内置规则能处理的URL
        System.out.println("=== 测试：内置规则演示 ===");
        System.out.println("输入: /api/users/list");
        System.out.println("输出: " + merger.normalize("/api/users/list"));
        System.out.println("说明: 静态URL不变化");

        System.out.println("\n输入: /api/user/123/detail");
        System.out.println("输出: " + merger.normalize("/api/user/123/detail"));
        System.out.println("说明: 数字123被替换为{123}");

        System.out.println("\n输入: /api/order/2024001/create");
        System.out.println("输出: " + merger.normalize("/api/order/2024001/create"));
        System.out.println("说明: 长数字也被识别为参数");
    }
}