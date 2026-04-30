package com.audit.apimerge.pipeline;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefaultMergerTest {
    @Test
    void shouldNormalizeNumericPathParam() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/user/{123}/detail", merger.normalize("/api/user/123/detail"));
    }

    @Test
    void shouldNormalizeAlphanumericPathParam() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/user/{abc123}/detail", merger.normalize("/api/user/abc123/detail"));
    }

    @Test
    void shouldNormalizeMultipleParams() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/{123}/{456}/items", merger.normalize("/api/123/456/items"));
    }

    @Test
    void shouldKeepStaticPartsUnchanged() {
        DefaultMerger merger = new DefaultMerger();
        assertEquals("/api/users/list", merger.normalize("/api/users/list"));
    }
}