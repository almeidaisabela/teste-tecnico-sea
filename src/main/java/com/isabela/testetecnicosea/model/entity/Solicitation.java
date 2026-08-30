package com.isabela.testetecnicosea.model.entity;

import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "solicitations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Solicitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SolicitationStatus status;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 20)
    private ServiceType serviceType;

    @Column(name = "title", length = 80)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "cep", length = 9)
    private String cep;

    @Column(name = "number", length = 20)
    private String number;

    @Column(name = "complement", length = 100)
    private String complement;

    @Column(name = "street", length = 150)
    private String street;

    @Column(name = "neighborhood", length = 100)
    private String neighborhood;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 2)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    private Priority priority;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "terms_accepted")
    private Boolean termsAccepted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "analyzed_by")
    private Integer analyzedBy;

    @Column(name = "analysis_comment", length = 1000)
    private String analysisComment;

}
