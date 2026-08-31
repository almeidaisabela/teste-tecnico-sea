package com.isabela.testetecnicosea.model.entity;

import com.isabela.testetecnicosea.model.enums.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "analyst_coverage_state")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalystCoverageState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 2)
    private State state;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
