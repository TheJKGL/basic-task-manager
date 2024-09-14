package com.taskmanager.controller;

import com.taskmanager.model.Status;
import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Task Controller", description = "The Task API")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Operation(summary = "Create a task", description = "Create task and return id of created entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created")
    })
    @PostMapping
    public ResponseEntity<String> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask.getUuid());
    }

    @Operation(summary = "Delete task by id", description = "Delete task by provided id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Not found - The task was not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTask(@PathVariable String uuid) {
        taskService.deleteTask(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update task status", description = "Update status of corresponding task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class)),
                    description = "Status successfully updated"),
            @ApiResponse(responseCode = "404",
                    content = @Content(),
                    description = "Not found - The task was not found")
    })
    @PutMapping("/{uuid}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable String uuid, @NotNull @RequestParam Status status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(uuid, status));
    }

    @Operation(summary = "Update task fields", description = "Update fields of corresponding task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Task.class)),
                    description = "Task fields successfully updated"),
            @ApiResponse(responseCode = "404",
                    content = @Content(),
                    description = "Not found - The task was not found")
    })
    @PatchMapping("/{uuid}")
    public ResponseEntity<Task> updateTaskFields(@PathVariable String uuid, @RequestBody Task updatedTask) {
        return ResponseEntity.ok(taskService.updateTaskFields(uuid, updatedTask));
    }

    @Operation(summary = "Get all tasks", description = "Get all available tasks from db")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Task.class))),
                    description = "All tasks successfully retrieved")
    })
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
}
