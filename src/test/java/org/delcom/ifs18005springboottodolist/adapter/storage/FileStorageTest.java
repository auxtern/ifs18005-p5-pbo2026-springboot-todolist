package org.delcom.ifs18005springboottodolist.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void storesFileWithExtensionAndResolvesPath() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        String relative = storage.store(
                "covers",
                "foto.png",
                new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))
        );

        assertTrue(relative.startsWith("covers/"));
        assertTrue(relative.endsWith(".png"));
        assertTrue(Files.exists(storage.resolve(relative)));
        assertEquals(tempDir.toAbsolutePath().normalize(), storage.root());
    }

    @Test
    void fallsBackExtensionWhenMissingOrTooLong() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        String noExt = storage.store("profiles", "tanpaext", new ByteArrayInputStream(new byte[]{1}));
        assertTrue(noExt.endsWith(".bin"));

        String longExt = storage.store(
                "profiles",
                "a." + "x".repeat(20),
                new ByteArrayInputStream(new byte[]{1})
        );
        assertTrue(longExt.endsWith(".bin"));

        String nullName = storage.store("profiles", null, new ByteArrayInputStream(new byte[]{1}));
        assertTrue(nullName.endsWith(".bin"));
    }
}
