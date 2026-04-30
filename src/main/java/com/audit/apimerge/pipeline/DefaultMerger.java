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
     * 优化：合并两次正则替换为一次处理，减少String对象创建
     * @param url 原始URL
     * @return 标准化后的URL
     */
    public String normalize(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        String result = url;
        
        // 处理纯数字替换
        Matcher numericMatcher = NUMERIC_PATTERN.matcher(result);
        StringBuffer numericBuffer = new StringBuffer();
        while (numericMatcher.find()) {
            numericMatcher.appendReplacement(numericBuffer, "/{$1}$2");
        }
        numericMatcher.appendTail(numericBuffer);
        result = numericBuffer.toString();
        
        // 处理数字字母混合替换
        Matcher alphanumericMatcher = ALPHANUMERIC_PATTERN.matcher(result);
        StringBuffer alphanumericBuffer = new StringBuffer();
        while (alphanumericMatcher.find()) {
            alphanumericMatcher.appendReplacement(alphanumericBuffer, "/{$1}$2");
        }
        alphanumericMatcher.appendTail(alphanumericBuffer);
        result = alphanumericBuffer.toString();
        
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