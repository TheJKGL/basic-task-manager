package com.taskmanager;

import com.taskmanager.exception.DuplicationException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnmodifiedException;
import com.taskmanager.model.Status;
import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    public void setup() {
        task = new Task();
        task.setUuid("123-uuid");
        task.setTitle("Sample Task");
        task.setStatus(Status.NEW);
    }

    @Test
    public void createTask_ShouldThrowException_WhenTitleExists() {
        when(taskRepository.existsByTitle(task.getTitle())).thenReturn(true);

        DuplicationException exception = assertThrows(DuplicationException.class, () -> {
            taskService.createTask(task);
        });

        assertEquals("Task with the same title already exists", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void createTask_ShouldSaveTask_WhenTitleDoesNotExist() {
        when(taskRepository.existsByTitle(task.getTitle())).thenReturn(false);
        when(taskRepository.save(task)).thenReturn(task);

        Task createdTask = taskService.createTask(task);

        assertNotNull(createdTask);
        verify(taskRepository).save(task);
    }

    @Test
    public void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findById(task.getUuid())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(task.getUuid());
        });

        assertEquals("Task Record Not Found By UUID: 123-uuid", exception.getMessage());
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    public void deleteTask_ShouldDeleteTask_WhenTaskExists() {
        when(taskRepository.findById(task.getUuid())).thenReturn(Optional.of(task));

        taskService.deleteTask(task.getUuid());

        verify(taskRepository).delete(task);
    }

    @Test
    public void updateTaskStatus_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findById(task.getUuid())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTaskStatus(task.getUuid(), Status.COMPLETED);
        });

        assertEquals("Task Record Not Found By UUID: 123-uuid", exception.getMessage());
    }

    @Test
    public void updateTaskStatus_ShouldUpdateStatus_WhenTaskExists() {
        when(taskRepository.findById(task.getUuid())).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task updatedTask = taskService.updateTaskStatus(task.getUuid(), Status.COMPLETED);

        assertEquals(Status.COMPLETED, updatedTask.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    public void updateTaskFields_ShouldThrowException_WhenUUIDIsModified() {
        Task updatedTask = new Task();
        updatedTask.setUuid("modified-uuid");

        UnmodifiedException exception = assertThrows(UnmodifiedException.class, () -> {
            taskService.updateTaskFields(task.getUuid(), updatedTask);
        });

        assertEquals("It is not allowed to modify UUID", exception.getMessage());
    }

    @Test
    public void updateTaskFields_ShouldUpdateFields_WhenValidData() {
        when(taskRepository.findById(task.getUuid())).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task Title");

        taskService.updateTaskFields(task.getUuid(), updatedTask);

        verify(taskRepository).save(task);
    }

    @Test
    public void getAllTasks_ShouldReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> tasks = taskService.getAllTasks();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        verify(taskRepository).findAll();
    }
}
