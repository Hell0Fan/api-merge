package com.audit.apimerge.progress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProgressManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldReturnZeroWhenFileNotExists() {
        ProgressManager pm = new ProgressManager(tempDir.resolve("nonexist.txt").toString());
        assertEquals(0, pm.load());
    }

    @Test
    void shouldSaveAndLoadLastProcessedId() {
        String filePath = tempDir.resolve("progress.txt").toString();
        ProgressManager pm = new ProgressManager(filePath);
        pm.save(12345);
        assertEquals(12345, pm.load());
    }
}