package com.isabela.testetecnicosea.repository;

import com.isabela.testetecnicosea.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
}
