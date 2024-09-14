package com.taskmanager.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
@Schema(description = "Entity that represents task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Schema(example = "Every day task", description = "Task title specified by client")
    @NotBlank(message = "Title is mandatory")
    private String title;

    @Schema(example = "Task description", description = "Task description specified by client")
    private String description;

    @Schema(example = "COMPLETED", description = "Task status specified by client")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status can't be null")
    private Status status;
}
