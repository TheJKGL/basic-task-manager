package com.taskmanager.service;

import com.taskmanager.exception.DuplicationException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnmodifiedException;
import com.taskmanager.model.Status;
import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(Task task) {
        if (taskRepository.existsByTitle(task.getTitle())) {
            throw new DuplicationException("Task with the same title already exists");
        }
        return taskRepository.save(task);
    }

    public void deleteTask(String uuid) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task Record Not Found By UUID: " + uuid));
        taskRepository.delete(task);
    }

    public Task updateTaskStatus(String uuid, Status status) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task Record Not Found By UUID: " + uuid));
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public Task updateTaskFields(String uuid, Task updatedTask) {
        if (updatedTask.getUuid() != null) {
            throw new UnmodifiedException("It is not allowed to modify UUID");
        }

        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task Record Not Found By UUID: " + uuid));
        PatcherUtils.patch(task, updatedTask);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}
