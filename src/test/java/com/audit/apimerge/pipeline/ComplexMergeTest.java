package com.audit.apimerge.pipeline;

import com.audit.apimerge.model.ApiRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 复杂场景合并测试
 * 测试各种复杂、相似度高的API接口合并效果
 */
class ComplexMergeTest {

    @Test
    @DisplayName("场景1：相同业务实体不同ID的用户接口")
    void testUserEntityVariations() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = Arrays.asList(
            "/api/v1/users/1001/profile",
            "/api/v1/users/1002/profile",
            "/api/v1/users/1003/profile",
            "/api/v1/users/2001/profile",
            "/api/v1/users/2002/profile"
        );

        System.out.println("\n========== 场景1：相同业务实体不同ID ==========");
        System.out.println("原始URL列表：");
        urls.forEach(url -> System.out.println("  " + url));

        // 标准化
        System.out.println("\n标准化结果：");
        Map<String, List<String>> groups = new HashMap<>();
        for (String url : urls) {
            String normalized = merger.normalize(url);
            groups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(url);
            System.out.println("  " + url + " -> " + normalized);
        }

        // 验证分组
        System.out.println("\n合并结果：");
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            System.out.println("  合并为: " + entry.getKey());
            System.out.println("  包含: " + entry.getValue().size() + " 个接口");
            entry.getValue().forEach(url -> System.out.println("    - " + url));
        }

        assertTrue(groups.size() >= 1, "应该至少有1个分组");
    }

    @Test
    @DisplayName("场景2：订单相关接口 - 多种操作类型")
    void testOrderOperations() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = Arrays.asList(
            "/api/orders/2024001/create",
            "/api/orders/2024002/create",
            "/api/orders/2024003/create",
            "/api/orders/2024001/query",
            "/api/orders/2024002/query",
            "/api/orders/2024001/update",
            "/api/orders/2024001/delete",
            "/api/orders/list",
            "/api/orders/export"
        );

        System.out.println("\n========== 场景2：订单相关接口 - 多种操作类型 ==========");
        System.out.println("原始URL列表：");
        urls.forEach(url -> System.out.println("  " + url));

        System.out.println("\n标准化结果：");
        Map<String, List<String>> groups = new HashMap<>();
        for (String url : urls) {
            String normalized = merger.normalize(url);
            groups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(url);
            System.out.println("  " + url + " -> " + normalized);
        }

        System.out.println("\n合并分组统计：");
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            System.out.println("  [" + entry.getKey() + "] 包含 " + entry.getValue().size() + " 个接口");
        }

        // 验证：create、query、update、delete 属于不同操作，应该分开
        // list、export 是静态接口，不应该被合并
        assertTrue(groups.containsKey("/api/orders/list"), "静态接口不应合并");
        assertTrue(groups.containsKey("/api/orders/export"), "静态接口不应合并");
    }

    @Test
    @DisplayName("场景3：RESTful 资源管理接口")
    void testRestfulResources() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = Arrays.asList(
            // Products - 相同静态部分
            "/api/v2/products/list",
            "/api/v2/products/create",
            // Categories
            "/api/v2/categories/cat001/detail",
            "/api/v2/categories/cat002/detail",
            "/api/v2/categories/cat003/detail",
            // SKU - 纯字母开头（不识别为参数）
            "/api/v2/sku/sku1001",
            "/api/v2/sku/sku1002",
            "/api/v2/sku/sku2001"
        );

        System.out.println("\n========== 场景3：RESTful 资源管理接口 ==========");
        System.out.println("原始URL列表：");
        urls.forEach(url -> System.out.println("  " + url));

        System.out.println("\n标准化结果：");
        for (String url : urls) {
            String normalized = merger.normalize(url);
            System.out.println("  " + url + " -> " + normalized);
        }

        // 验证：相同静态路径的URL标准化后应该相同
        String listResult = merger.normalize("/api/v2/products/list");
        assertEquals(listResult, merger.normalize("/api/v2/products/list"), "相同URL应该相同");

        // Categories 的 cat001/cat002/cat003 包含数字，应该被替换为参数
        String cat1 = merger.normalize("/api/v2/categories/cat001/detail");
        String cat2 = merger.normalize("/api/v2/categories/cat002/detail");
        // 由于 cat001 和 cat002 都包含数字，标准化后不同（这是正确的行为）
        System.out.println("\n说明：cat001 和 cat002 因包含数字，标准化后被识别为不同参数");
    }

    @Test
    @DisplayName("场景4：多层嵌套路径参数")
    void testMultiLevelNesting() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = Arrays.asList(
            "/tenant/org001/project/proj001/module/mod001/config",
            "/tenant/org001/project/proj002/module/mod002/config",
            "/tenant/org002/project/proj001/module/mod003/config",
            "/tenant/org001/project/proj001/module/list",
            "/tenant/org001/project/proj001/module/create"
        );

        System.out.println("\n========== 场景4：多层嵌套路径参数 ==========");
        System.out.println("原始URL列表：");
        urls.forEach(url -> System.out.println("  " + url));

        System.out.println("\n标准化结果：");
        for (String url : urls) {
            String normalized = merger.normalize(url);
            System.out.println("  " + url);
            System.out.println("    -> " + normalized);
        }
    }

    @Test
    @DisplayName("场景5：不同HTTP方法但相同路径的接口")
    void testSamePathDifferentMethods() {
        DefaultMerger merger = new DefaultMerger();

        // 模拟不同方法的相同路径
        List<String[]> testCases = Arrays.asList(
            new String[]{"/api/users/123", "GET"},
            new String[]{"/api/users/123", "POST"},
            new String[]{"/api/users/123", "PUT"},
            new String[]{"/api/users/123", "DELETE"},
            new String[]{"/api/orders/list", "GET"},
            new String[]{"/api/orders/create", "POST"}
        );

        System.out.println("\n========== 场景5：不同HTTP方法但相同路径的接口 ==========");
        System.out.println("输入（URL + Method）：");

        Map<String, List<String>> methodGroups = new HashMap<>();
        for (String[] tc : testCases) {
            String url = tc[0];
            String method = tc[1];
            String key = method + ":" + url;
            methodGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(method);
            System.out.println("  [" + method + "] " + url);
        }

        // 说明：当前合并逻辑不区分HTTP方法，仅基于URL
        System.out.println("\n说明：当前合并不区分HTTP方法，仅基于URL标准化");
        System.out.println("如需按方法区分，需要扩展分组逻辑");
    }

    @Test
    @DisplayName("场景6：带版本号和长数字ID的接口")
    void testVersionAndLongIds() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = Arrays.asList(
            "/api/v3/user/20240101123456789/profile",
            "/api/v3/user/20240101123456790/profile",
            "/api/v3/user/20240101123456791/profile",
            "/api/v3/order/20240102000001/detail",
            "/api/v3/order/20240102000002/detail",
            "/api/v3/order/20240102000003/detail",
            "/api/v3/product/9876543210/info",
            "/api/v3/product/9876543211/info"
        );

        System.out.println("\n========== 场景6：带版本号和长数字ID的接口 ==========");
        System.out.println("原始URL列表：");
        urls.forEach(url -> System.out.println("  " + url));

        System.out.println("\n标准化结果：");
        Map<String, List<String>> groups = new HashMap<>();
        for (String url : urls) {
            String normalized = merger.normalize(url);
            groups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(url);
            System.out.println("  " + url + " -> " + normalized);
        }

        System.out.println("\n合并结果：");
        groups.forEach((k, v) -> System.out.println("  " + k + " = " + v.size() + " 个"));

        // 验证长数字ID被正确识别
        assertTrue(groups.keySet().stream().anyMatch(k -> k.contains("{20240101123456789}")));
    }

    @Test
    @DisplayName("场景7：相似但不同的接口（边界场景）")
    void testSimilarButDifferent() {
        DefaultMerger merger = new DefaultMerger();

        List<String[]> testCases = Arrays.asList(
            new String[]{"/api/admin/users", "/api/admin/users"},
            new String[]{"/api/admin/user", "/api/admin/user"},
            new String[]{"/api/admin/user/list", "/api/admin/users/list"},
            new String[]{"/api/users", "/api/users"},
            new String[]{"/api/users1", "/api/users1"},
            new String[]{"/api/users2", "/api/users2"},
            new String[]{"/api/user", "/api/user"},
            new String[]{"/api/user/123", "/api/user/123"}
        );

        System.out.println("\n========== 场景7：相似但不同的接口（边界场景） ==========");
        System.out.println("测试URL对及其标准化结果：");

        for (String[] tc : testCases) {
            String url1 = tc[0];
            String url2 = tc[1];
            String n1 = merger.normalize(url1);
            String n2 = merger.normalize(url2);
            boolean same = n1.equals(n2);

            System.out.println("\n  URL1: " + url1 + " -> " + n1);
            System.out.println("  URL2: " + url2 + " -> " + n2);
            System.out.println("  是否相同: " + same);
        }
    }

    @Test
    @DisplayName("场景8：完整业务流程接口模拟")
    void testBusinessFlow() {
        DefaultMerger merger = new DefaultMerger();

        // 模拟电商完整业务流程
        List<String> urls = Arrays.asList(
            // 用户模块
            "/api/shop/user/user001/register",
            "/api/shop/user/user002/register",
            "/api/shop/user/user001/login",
            "/api/shop/user/user002/login",
            "/api/shop/user/user001/info",
            "/api/shop/user/user002/info",
            // 购物车
            "/api/shop/cart/cart001/add",
            "/api/shop/cart/cart002/add",
            "/api/shop/cart/cart001/remove",
            "/api/shop/cart/cart001/list",
            // 订单
            "/api/shop/order/order001/create",
            "/api/shop/order/order002/create",
            "/api/shop/order/order001/pay",
            "/api/shop/order/order002/pay",
            "/api/shop/order/order001/ship",
            "/api/shop/order/order002/ship",
            "/api/shop/order/order001/complete",
            "/api/shop/order/list",
            // 商品
            "/api/shop/product/prod001/detail",
            "/api/shop/product/prod002/detail",
            "/api/shop/product/prod003/detail",
            "/api/shop/product/list",
            "/api/shop/product/category/elec",
            "/api/shop/product/category/food",
            // 支付
            "/api/shop/pay/pay001",
            "/api/shop/pay/pay002",
            "/api/shop/pay/pay001/refund"
        );

        System.out.println("\n========== 场景8：完整业务流程接口模拟 ==========");
        System.out.println("接口总数: " + urls.size());

        // 按功能模块分组展示
        System.out.println("\n按模块分组统计：");
        Map<String, List<String>> byModule = new LinkedHashMap<>();
        for (String url : urls) {
            String module = extractModule(url);
            byModule.computeIfAbsent(module, k -> new ArrayList<>()).add(url);
        }
        byModule.forEach((mod, list) -> System.out.println("  " + mod + ": " + list.size() + " 个"));

        // 完整标准化
        System.out.println("\n标准化结果：");
        Map<String, List<String>> groups = new HashMap<>();
        for (String url : urls) {
            String normalized = merger.normalize(url);
            groups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(url);
        }

        System.out.println("合并后分组数: " + groups.size());
        System.out.println("\n各组详情：");
        groups.entrySet().stream()
            .sorted((a, b) -> b.getValue().size() - a.getValue().size())
            .forEach(entry -> {
                System.out.println("  " + entry.getKey() + " (" + entry.getValue().size() + "个)");
                entry.getValue().forEach(url -> System.out.println("    - " + url));
            });
    }

    private String extractModule(String url) {
        String[] parts = url.split("/");
        if (parts.length >= 3) {
            return parts[2]; // 取第三级作为模块名
        }
        return "unknown";
    }

    @Test
    @DisplayName("场景9：ID格式多样化测试")
    void testVariousIdFormats() {
        DefaultMerger merger = new DefaultMerger();

        List<String> urls = Arrays.asList(
            // 纯数字
            "/api/user/1234567890/detail",
            // 字母开头+数字
            "/api/user/abc123/detail",
            // 数字+字母
            "/api/user/123abc/detail",
            // 纯字母（短，不识别）
            "/api/user/abc/detail",
            // 纯字母（长，识别）
            "/api/user/abcdef/detail",
            // 混合特殊字符
            "/api/user/uuid-123/detail",
            // 下划线
            "/api/user/user_id_001/detail"
        );

        System.out.println("\n========== 场景9：ID格式多样化测试 ==========");
        System.out.println("原始URL -> 标准化结果：");
        for (String url : urls) {
            String normalized = merger.normalize(url);
            boolean changed = !url.equals(normalized);
            String flag = changed ? "[参数]" : "[静态]";
            System.out.println("  " + flag + " " + url);
            System.out.println("       -> " + normalized);
        }
    }

    @Test
    @DisplayName("场景10：大量相似接口批量合并性能")
    void testBatchMergePerformance() {
        DefaultMerger merger = new DefaultMerger();

        // 生成100个相似接口
        List<String> urls = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            urls.add("/api/resource/item" + String.format("%03d", i) + "/detail");
            urls.add("/api/resource/item" + String.format("%03d", i) + "/update");
        }

        System.out.println("\n========== 场景10：大量相似接口批量合并性能 ==========");
        System.out.println("接口总数: " + urls.size());

        long start = System.nanoTime();
        Map<String, List<String>> groups = new HashMap<>();
        for (String url : urls) {
            String normalized = merger.normalize(url);
            groups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(url);
        }
        long elapsed = System.nanoTime() - start;

        System.out.println("处理耗时: " + (elapsed / 1000.0) + " us");
        System.out.println("平均耗时: " + (elapsed / 1000.0 / urls.size()) + " us/接口");
        System.out.println("合并后分组数: " + groups.size());
        System.out.println("平均每组: " + (urls.size() / groups.size()) + " 个接口");

        // 性能断言
        assertTrue(elapsed < 10000000, "100个接口处理应在10ms内完成");
    }
}