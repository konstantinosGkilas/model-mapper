package com.kgkilas.mapping.mapper;

import com.kgkilas.mapping.annotation.IgnoreField;
import com.kgkilas.mapping.strategy.NullHandlingStrategy;
import com.kgkilas.mapping.strategy.SetToNullStrategy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A generic mapper class that handles conversion between entity and DTO objects.
 * It also provides methods for patching DTOs and handling null values based on strategy.
 *
 * @param <E> the type of the entity
 * @param <D> the type of the DTO
 */
public class GenericMapper<E, D> {
    private static final Logger logger = LoggerFactory.getLogger(GenericMapper.class);

    private final ModelMapper modelMapper;
    private final Class<E> entityClass;
    private final Class<D> dtoClass;
    private final Validator validator;

    @Setter
    private NullHandlingStrategy nullHandlingStrategy = new SetToNullStrategy(); // Default strategy

    public GenericMapper(ModelMapper modelMapper, Class<E> entityClass, Class<D> dtoClass) {
        this.modelMapper = modelMapper;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Converts an entity to a DTO and validates it.
     *
     * @param entity the entity to convert
     * @return the DTO
     */
    public D toDTO(E entity) {
        D dto = modelMapper.map(entity, dtoClass);
        validate(dto);
        return dto;
    }

    /**
     * Converts a list of entities to a list of DTOs and validates each.
     *
     * @param entityList the list of entities
     * @return the list of DTOs
     */
    public List<D> toDTO(List<E> entityList) {
        return entityList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a DTO to an entity and validates it.
     *
     * @param dto the DTO to convert
     * @return the entity
     */
    public E toEntity(D dto) {
        validate(dto);
        return modelMapper.map(dto, entityClass);
    }

    /**
     * Converts a list of DTOs to a list of entities and validates each.
     *
     * @param dtoList the list of DTOs
     * @return the list of entities
     */
    public List<E> toEntity(List<D> dtoList) {
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Patches the existingDTO with values from updateDTO. If a field is null in updateDTO, it uses the nullHandlingStrategy.
     *
     * @param updateDTO      the DTO with updated values
     * @param existingDTO    the existing DTO to be patched
     * @param nullFieldsNames the list of fields to be set to null if they are null in updateDTO
     */
    public void patch(Object updateDTO, Object existingDTO, List<String> nullFieldsNames) {
        if (updateDTO == null || existingDTO == null) {
            throw new IllegalArgumentException("Both updateDTO and existingDTO must be non-null");
        }
        updateFields(updateDTO, existingDTO, nullFieldsNames);
    }

    private void updateFields(Object updateDTO, Object existingDTO, List<String> nullFieldsNames) {
        Field[] fields = updateDTO.getClass().getDeclaredFields();
        for (Field field : fields) {
            processField(updateDTO, existingDTO, field, nullFieldsNames);
        }
    }

    private void processField(Object updateDTO, Object existingDTO, Field field, List<String> nullFieldsNames) {
        try {
            field.setAccessible(true);
            if (!shouldSkipField(field)) {
                Optional<Object> value = Optional.ofNullable(field.get(updateDTO));
                Field existingDTOField = getField(existingDTO, field.getName());
                existingDTOField.setAccessible(true);

                if (value.isPresent()) {
                    updateFieldValue(existingDTO, existingDTOField, value.get(), nullFieldsNames);
                } else if (nullFieldsNames != null && nullFieldsNames.contains(field.getName())) {
                    nullHandlingStrategy.handle(field, updateDTO, existingDTO);
                    logger.debug("Field '{}' set to null", field.getName());
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.error("Error accessing field '{}'", field.getName(), e);
        }
    }

    private boolean shouldSkipField(Field field) {
        if (field.isAnnotationPresent(IgnoreField.class)) {
            logger.debug("Field '{}' is marked with @IgnoreField, skipping", field.getName());
            return true;
        }
        return false;
    }

    private Field getField(Object object, String fieldName) throws NoSuchFieldException {
        return object.getClass().getDeclaredField(fieldName);
    }

    private void updateFieldValue(Object existingDTO, Field existingDTOField, Object value, List<String> nullFieldsNames) throws IllegalAccessException {
        if (isComplexObject(value)) {
            Optional<Object> existingValue = Optional.ofNullable(existingDTOField.get(existingDTO));
            if (existingValue.isPresent()) {
                patch(value, existingValue.get(), nullFieldsNames);
            } else {
                existingDTOField.set(existingDTO, value);
            }
        } else {
            existingDTOField.set(existingDTO, value);
        }
        logger.debug("Field '{}' updated with value '{}'", existingDTOField.getName(), value);
    }

    private boolean isComplexObject(Object value) {
        return !isPrimitiveOrWrapper(value.getClass());
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.equals(String.class) ||
                clazz.equals(Integer.class) || clazz.equals(Long.class) ||
                clazz.equals(Double.class) || clazz.equals(Float.class) ||
                clazz.equals(Boolean.class) || clazz.equals(Character.class) ||
                clazz.equals(Byte.class) || clazz.equals(Short.class);
    }

    private void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String violationMessages = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("\n"));
            throw new IllegalArgumentException("Validation failed:\n" + violationMessages);
        }
    }
}