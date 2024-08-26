package com.kgkilas.mapping.strategy;

import java.lang.reflect.Field;

public class SkipNullStrategy implements NullHandlingStrategy {
    @Override
    public void handle(Field field, Object updateDTO, Object existingDTO) {
        // Do nothing, skip null fields
    }
}
