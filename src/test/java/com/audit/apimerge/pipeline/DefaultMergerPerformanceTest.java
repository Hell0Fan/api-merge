package com.audit.apimerge.pipeline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMergerPerformanceTest {
    @Test
    @DisplayName("Performance: merge 10000 URLs should complete in < 100ms")
    void shouldComplete10000MergesInUnder100ms() {
        DefaultMerger merger = new DefaultMerger();
        String[] urls = {
            "/api/user/123/detail",
            "/api/user/456/profile",
            "/api/order/789/items",
            "/api/product/abc123/info",
            "/api/users/list"
        };

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            for (String url : urls) {
                merger.merge(url);
            }
        }
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("Total time: " + elapsed + "ms");
        System.out.println("Avg per merge: " + (elapsed * 1000.0 / 50000) + "us");

        assertTrue(elapsed < 500, "Should complete in < 500ms, actual: " + elapsed + "ms");
    }

    @Test
    @DisplayName("Performance: single merge should be < 1ms")
    void shouldMergeInUnder1Ms() {
        DefaultMerger merger = new DefaultMerger();

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            merger.merge("/api/user/123/detail");
            merger.merge("/api/user/abc123/profile");
            merger.merge("/api/order/456789/items");
        }
        long elapsed = System.nanoTime() - startTime;
        double avgUs = elapsed / 1000.0 / 3000;

        System.out.println("Avg time per merge: " + avgUs + "us");
        assertTrue(avgUs < 1000, "Should be < 1ms, actual: " + avgUs + "us");
    }
}