package com.diploma.model;

import jakarta.persistence.*;
import lombok.*;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Id
    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "type")
    private String type;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Type(JsonType.class)
    @Column(name = "result", columnDefinition = "json", nullable = false)
    private Map<String, Object> result;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}