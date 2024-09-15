package com.taskmanager.service;

import com.taskmanager.exception.PatcherServiceException;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.Field;

/**
 * This service helps us manage patch update of resource.
 * Here we can specify logic for each entity which should be partially updated.
 */
public class PatcherUtils {
    
    public static <T> void patch(T existingObj, T incompleteObj) {
        try {
            Class<?> clazz = existingObj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(incompleteObj);
                if (value == null || (value instanceof String str && Strings.isBlank(str))) {
                    continue;
                }
                field.set(existingObj, value);
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new PatcherServiceException(e.getMessage());
        }
    }
}