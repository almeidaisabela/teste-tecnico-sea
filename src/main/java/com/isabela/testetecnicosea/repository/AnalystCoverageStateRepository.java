package com.isabela.testetecnicosea.repository;

import com.isabela.testetecnicosea.model.entity.AnalystCoverageState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AnalystCoverageStateRepository extends JpaRepository<AnalystCoverageState, Integer> {

    List<AnalystCoverageState> findByUserId(Integer userId);

    boolean existsByUserIdAndState(Integer userId, String state);

}
