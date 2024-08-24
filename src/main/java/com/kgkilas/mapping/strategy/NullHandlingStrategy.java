package com.kgkilas.mapping.strategy;

import java.lang.reflect.Field;

// Null Handling Strategies
public interface NullHandlingStrategy {
    void handle(Field field, Object updateDTO, Object existingDTO) throws IllegalAccessException, NoSuchFieldException;
}
