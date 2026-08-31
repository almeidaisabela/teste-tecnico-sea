package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.AnalystDecisionRequestDTO;
import com.isabela.testetecnicosea.model.entity.AnalystCoverageState;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.repository.AnalystCoverageStateRepository;
import com.isabela.testetecnicosea.repository.SolicitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalystSolicitationService {

    private final SolicitationRepository solicitationRepository;
    private final AnalystCoverageStateRepository coverageRepository;

    public Solicitation findById(Integer id, User analyst) {
        Solicitation solicitation = solicitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        checkCoverage(solicitation, analyst);

        return solicitation;
    }

    public Solicitation start(Integer id, User analyst) {
        Solicitation solicitation = findById(id, analyst);

        if (solicitation.getStatus() != SolicitationStatus.SUBMITTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Só é possível iniciar análise de solicitações com status SUBMITTED"
            );
        }

        solicitation.setStatus(SolicitationStatus.IN_REVIEW);
        solicitation.setUpdatedAt(LocalDateTime.now());

        return solicitationRepository.save(solicitation);
    }


    public Solicitation decide(Integer id, AnalystDecisionRequestDTO request, User analyst) {
        Solicitation solicitation = findById(id, analyst);

        if (solicitation.getStatus() != SolicitationStatus.SUBMITTED
                && solicitation.getStatus() != SolicitationStatus.IN_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Só é possível decidir solicitações com status SUBMITTED ou IN_REVIEW"
            );
        }

        SolicitationStatus newStatus = switch (request.decision()) {
            case APPROVE -> SolicitationStatus.APPROVED;
            case REJECT -> SolicitationStatus.REJECTED;
        };

        solicitation.setStatus(newStatus);
        solicitation.setAnalysisComment(request.comment());
        solicitation.setAnalyzedBy(analyst.getId());
        solicitation.setAnalyzedAt(LocalDateTime.now());
        solicitation.setUpdatedAt(LocalDateTime.now());

        return solicitationRepository.save(solicitation);
    }


    private void checkCoverage(Solicitation solicitation, User analyst) {
        List<AnalystCoverageState> coverage = coverageRepository.findByUserId(analyst.getId());

        boolean covers = coverage.stream()
                .anyMatch(c -> c.getState() == solicitation.getState());

        if (!covers) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem cobertura para o estado desta solicitação"
            );
        }
    }

}


