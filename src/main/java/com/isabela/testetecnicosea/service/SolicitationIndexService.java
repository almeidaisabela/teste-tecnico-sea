package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.document.SolicitationDocument;
import com.isabela.testetecnicosea.repository.SolicitationSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitationIndexService {

    private final SolicitationSearchRepository solicitationSearchRepository;

    public void index(Solicitation solicitation) {
        try {
            SolicitationDocument document = toDocument(solicitation);
            solicitationSearchRepository.save(document);
        } catch (Exception exception) {
            log.error("Falha ao indexar solicitation {} no Elasticsearch", solicitation.getId(), exception);
        }
    }

    private SolicitationDocument toDocument(Solicitation s) {
        SolicitationDocument document = new SolicitationDocument();
        document.setId(String.valueOf(s.getId()));
        document.setClientId(s.getClientId());
        document.setStatus(s.getStatus());
        document.setServiceType(s.getServiceType());
        document.setTitle(s.getTitle());
        document.setDescription(s.getDescription());
        document.setState(s.getState());
        document.setCity(s.getCity());
        document.setPriority(s.getPriority());
        document.setCreatedAt(s.getCreatedAt());
        document.setSubmittedAt(s.getSubmittedAt());
        return document;
    }

}
