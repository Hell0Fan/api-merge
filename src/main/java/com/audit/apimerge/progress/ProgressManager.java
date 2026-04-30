package com.audit.apimerge.progress;

import java.io.*;

/**
 * 断点续传管理器
 * 负责保存和加载上次处理的最后一条记录的ID
 * 格式：last_processed_id=12345
 */
public class ProgressManager {
    private final String filePath;  // 进度文件路径

    /**
     * 构造函数
     * @param filePath 进度文件路径
     */
    public ProgressManager(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 加载上次保存的进度
     * @return 上次处理的最大ID，不存在则返回0
     */
    public long load() {
        File file = new File(filePath);
        if (!file.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && line.startsWith("last_processed_id=")) {
                return Long.parseLong(line.split("=")[1]);
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    /**
     * 保存当前进度
     * @param id 当前处理的最大ID
     */
    public void save(long id) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("last_processed_id=" + id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save progress", e);
        }
    }
}