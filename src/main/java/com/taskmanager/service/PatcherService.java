package com.taskmanager.service;

import com.taskmanager.exception.PatcherServiceException;
import com.taskmanager.model.Task;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

/**
 * This service helps us manage patch update of resource.
 * Here we can specify logic for each entity which should be partially updated.
 */
@Service
public class PatcherService {
    
    public static void taskPatcher(Task existingTask, Task incompleteTask) {
        try {
            //GET THE COMPILED VERSION OF THE CLASS
            Class<?> taskClass = Task.class;
            Field[] taskFields = taskClass.getDeclaredFields();
            for (Field field : taskFields) {
                //CANT ACCESS IF THE FIELD IS PRIVATE
                field.setAccessible(true);

                //CHECK IF THE VALUE OF THE FIELD IS NOT NULL, IF NOT UPDATE EXISTING INTERN
                Object value = field.get(incompleteTask);
                if (value != null) {
                    field.set(existingTask, value);
                }
                //MAKE THE FIELD PRIVATE AGAIN
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new PatcherServiceException(e.getMessage());
        }
    }
}