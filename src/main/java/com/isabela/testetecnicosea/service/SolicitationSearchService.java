package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.SolicitationSearchRequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationSearchResponseDTO;
import com.isabela.testetecnicosea.model.document.SolicitationDocument;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.State;
import com.isabela.testetecnicosea.model.enums.UserRole;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.repository.AnalystCoverageStateRepository;
import com.isabela.testetecnicosea.repository.SolicitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SolicitationSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AnalystCoverageStateRepository coverageRepository;
    private final SolicitationRepository solicitationRepository;
    private final SolicitationMapper solicitationMapper;

    public SolicitationSearchResponseDTO search(SolicitationSearchRequestDTO request, User requester) {
        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 20;

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page deve ser >= 0");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size deve ser >= 1");
        }

        Criteria criteria = new Criteria();

        if (request.q() != null && !request.q().isBlank()) {
            criteria = criteria.and(
                    new Criteria("title").contains(request.q())
                            .or(new Criteria("description").contains(request.q()))
            );
        }

        if (request.status() != null && !request.status().isEmpty()) {
            criteria = criteria.and(new Criteria("status").in(request.status()));
        }

        if (request.serviceType() != null) {
            criteria = criteria.and(new Criteria("serviceType").is(request.serviceType()));
        }

        if (request.priority() != null) {
            criteria = criteria.and(new Criteria("priority").is(request.priority()));
        }

        State effectiveState = resolveStateFilter(request.state(), requester);
        if (effectiveState != null) {
            criteria = criteria.and(new Criteria("state").is(effectiveState));
        } else if (requester.getRole() == UserRole.ANALYST) {
            List<State> coverage = coverageRepository.findByUserId(requester.getId())
                    .stream().map(c -> c.getState()).toList();
            if (coverage.isEmpty()) {
                return new SolicitationSearchResponseDTO(List.of(), page, size, 0);
            }
            criteria = criteria.and(new Criteria("state").in(coverage));
        }

        if (request.dateFrom() != null || request.dateTo() != null) {
            Criteria dateCriteria = new Criteria("submittedAt");
            if (request.dateFrom() != null && request.dateTo() != null) {
                dateCriteria = dateCriteria.between(request.dateFrom(), request.dateTo());
            } else if (request.dateFrom() != null) {
                dateCriteria = dateCriteria.greaterThanEqual(request.dateFrom());
            } else {
                dateCriteria = dateCriteria.lessThanEqual(request.dateTo());
            }
            criteria = criteria.and(dateCriteria);
        }

        Sort sort = parseSort(request.sort());
        CriteriaQuery query = new CriteriaQuery(criteria, PageRequest.of(page, size, sort));

        SearchHits<SolicitationDocument> hits = elasticsearchOperations.search(query, SolicitationDocument.class);

        List<Integer> ids = hits.stream()
                .map(hit -> Integer.valueOf(hit.getContent().getId()))
                .toList();

        List<com.isabela.testetecnicosea.model.entity.Solicitation> entities = solicitationRepository.findAllById(ids);
        var items = entities.stream().map(solicitationMapper::toResponse).toList();
        return new SolicitationSearchResponseDTO(items, page, size, hits.getTotalHits());
    }


    private State resolveStateFilter(State requestedState, User requester) {
        if (requester.getRole() == UserRole.ADMIN) {
            return requestedState;
        }

        if (requester.getRole() == UserRole.ANALYST) {
            List<State> coverage = coverageRepository.findByUserId(requester.getId())
                    .stream().map(c -> c.getState()).toList();

            if (requestedState != null) {
                if (!coverage.contains(requestedState)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Você não tem cobertura para o estado " + requestedState);
                }
                return requestedState;
            }
            return null;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem acesso à busca");
    }


    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "submittedAt");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

}
