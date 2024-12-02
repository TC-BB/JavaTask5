package com.example;

import com.example.beans.SomeBean;
import com.example.implementations.OtherImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {

    private static final String ORIGINAL_CONFIG = """
            com.example.interfaces.SomeInterface=com.example.implementations.SomeImpl
            com.example.interfaces.SomeOtherInterface=com.example.implementations.SODoer
            """;

    private Path tempDir;
    private Path configFilePath;


    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        this.tempDir = tempDir;
        configFilePath = tempDir.resolve("config.properties");
        Files.writeString(configFilePath, ORIGINAL_CONFIG);
    }

    @Test
    void testInjectionWithOtherImpl() throws IOException {
        modifyConfig("com.example.interfaces.SomeInterface", OtherImpl.class.getName());
        SomeBean sb = new Injector().inject(new SomeBean(), configFilePath.toString()); // Передаем путь к файлу
        sb.foo(); // Output should be BC
    }

    @Test
    void testInjectionWithMissingImplementation() throws IOException {
        modifyConfig("com.example.interfaces.SomeInterface", null);
        assertThrows(RuntimeException.class, () -> new Injector().inject(new SomeBean(), configFilePath.toString())); // Передаем путь к файлу
    }


    @Test
    void testInjectionWithInvalidClassName() throws IOException {
        modifyConfig("com.example.interfaces.SomeInterface", "com.example.NonExistentClass");
        assertThrows(RuntimeException.class, () -> new Injector().inject(new SomeBean(), configFilePath.toString())); // Передаем путь к файлу
    }

    // Этот тест теперь работает корректно, т.к. используется временный файл
    @Test
    void testInjectionWithNoPropertiesFile() {
        assertThrows(RuntimeException.class, () -> new Injector().inject(new SomeBean(), "nonexistent_file.properties"));
    }


    private void modifyConfig(String key, String value) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath.toFile())) {
            properties.load(fis);
            if (value == null) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value);
            }
            try (FileOutputStream fos = new FileOutputStream(configFilePath.toFile())) {
                properties.store(fos, null);
            }
        }
    }


    @AfterEach
    void tearDown() {
        // Временный каталог удаляется автоматически после теста
    }
}