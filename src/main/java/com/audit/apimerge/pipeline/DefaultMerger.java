package com.audit.apimerge.pipeline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认合并算法
 * 用于当内置规则无法匹配时，自动识别URL中的参数并标准化
 *
 * 参数识别规则：
 * 1. 纯数字：/api/user/123/detail → /api/user/{123}/detail
 * 2. 数字字母混合：/api/user/abc123/detail → /api/user/{abc123}/detail
 */
public class DefaultMerger {
    /** 匹配纯数字路径段，如 /123/ 或 /123 */
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("/(\\d+)(/|$)");
    /** 匹配数字字母混合的路径段（至少包含一个数字），如 /abc123/ 或 /abc123 */
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("/([a-zA-Z0-9]*\\d[a-zA-Z0-9]*)(/|$)");

    /**
     * 标准化URL：将路径中的参数替换为占位符
     * @param url 原始URL
     * @return 标准化后的URL
     */
    public String normalize(String url) {
        String result = url;
        // 替换纯数字
        result = NUMERIC_PATTERN.matcher(result).replaceAll("/{$1}$2");
        // 替换数字字母混合
        result = ALPHANUMERIC_PATTERN.matcher(result).replaceAll("/{$1}$2");
        return result;
    }

    /**
     * 合并处理（标准化）
     * @param url 原始URL
     * @return 标准化后的URL
     */
    public String merge(String url) {
        return normalize(url);
    }
}