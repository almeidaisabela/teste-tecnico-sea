package com.isabela.testetecnicosea.repository;

import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SolicitationRepository extends JpaRepository<Solicitation, Integer> {

    List<Solicitation> findByClientId(Integer clientId);

    List<Solicitation> findByClientIdAndStatus(Integer clientId, SolicitationStatus status);

}
