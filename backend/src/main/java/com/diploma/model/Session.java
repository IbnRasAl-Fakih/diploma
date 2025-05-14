package com.diploma.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}