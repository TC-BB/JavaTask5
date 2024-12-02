// Лабораторная работа №5 по языку программирования Java. Выполнил: Фефелов Дмитрий, 3 курс, 3 группа

/**
 * Задача: Рефлексия. 
 * Нужно:
 * 1. Создать аннотацию @AutoInjectable 
 * 2. Разобраться с классом Properties 
 * 3. Создать класс Injector в параметризированный котором был бы метод inject, который принимал бы в качестве параметра объект любого класса и, 
 * используя механизмы рефлексии осуществлял поиск полей, помеченных этой аннотацией(в качестве типа поля используются некоторый интерфейс), 
 * и осуществлял бы инициализацию этих полей экземплярами классов, которые указаны в качестве реализации соответствующего интерфейса настроек(properites)
 * */
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







