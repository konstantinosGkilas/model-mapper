package com.kgkilas.mapping.strategy;

import java.lang.reflect.Field;

public class SetToNullStrategy implements NullHandlingStrategy {
    @Override
    public void handle(Field field, Object updateDTO, Object existingDTO) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);
        Field existingDTOField = existingDTO.getClass().getDeclaredField(field.getName());
        existingDTOField.setAccessible(true);
        existingDTOField.set(existingDTO, null);
    }
}
