package com.isabela.testetecnicosea.model.entity;

import com.isabela.testetecnicosea.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "audit_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole role;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
