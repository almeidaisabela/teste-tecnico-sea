package com.isabela.testetecnicosea.repository;

import com.isabela.testetecnicosea.model.document.SolicitationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SolicitationSearchRepository extends ElasticsearchRepository<SolicitationDocument, String> {
}
