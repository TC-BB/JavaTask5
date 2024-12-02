package com.example;

import com.example.annotations.AutoInjectable;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public class Injector {
    public <T> T inject(T obj, String propertiesFilePath) { // Добавили параметр для пути к файлу свойств
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFilePath)) { // Используем переданный путь
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file: " + e.getMessage(), e);
        }

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoInjectable.class)) {
                String className = properties.getProperty(field.getType().getName());
                if (className == null || className.trim().isEmpty()) {
                    throw new RuntimeException("No implementation found for: " + field.getType().getName());
                }
                try {
                    Class<?> implClass = Class.forName(className);
                    Object implInstance = implClass.getDeclaredConstructor().newInstance();
                    field.setAccessible(true);
                    field.set(obj, implInstance);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Class not found: " + className, e);
                } catch (Exception e) {
                    throw new RuntimeException("Error injecting dependency: " + e.getMessage(), e);
                }
            }
        }
        return obj;
    }
}







