package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.SolicitationStep1RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep2RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep3RequestDTO;
import com.isabela.testetecnicosea.model.dto.ViaCepResponseDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.repository.SolicitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SolicitationService {

    private final SolicitationRepository solicitationRepository;
    private final SolicitationMapper solicitationMapper;
    private final CepService cepService;

    public Solicitation create(User client) {
        Solicitation solicitation = solicitationMapper.toNewEntity(client.getId());
        return solicitationRepository.save(solicitation);
    }


    public Solicitation saveStep1(Integer id, SolicitationStep1RequestDTO request, User client) {
        Solicitation solicitation = findOwnedDraft(id, client);
        solicitationMapper.updateStep1(request, solicitation);

        if (solicitation.getCurrentStep() < 1) {
            solicitation.setCurrentStep(1);
        }

        solicitation.setUpdatedAt(LocalDateTime.now());
        return solicitationRepository.save(solicitation);
    }


    public Solicitation saveStep2(Integer id, SolicitationStep2RequestDTO request, User client) {
        Solicitation solicitation = findOwnedDraft(id, client);
        solicitationMapper.updateStep2(request, solicitation);

        if (solicitation.getCurrentStep() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "É necessário concluir o Step 1 antes de preencher o Step 2"
            );
        }

        ViaCepResponseDTO adress = cepService.findAdress(request.cep());

        solicitation.setStreet(adress.logradouro());
        solicitation.setNeighborhood(adress.bairro());
        solicitation.setCity(adress.localidade());
        solicitation.setState(adress.uf());

        if (solicitation.getCurrentStep() < 2) {
            solicitation.setCurrentStep(2);
        }

        solicitation.setUpdatedAt(LocalDateTime.now());
        return solicitationRepository.save(solicitation);
    }


    public Solicitation saveStep3(Integer id, SolicitationStep3RequestDTO request, User client) {
        Solicitation solicitation = findOwnedDraft(id, client);

        if (solicitation.getCurrentStep() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "É necessário concluir o Step 2 antes de preencher o Step 3"
            );
        }

        if (request.priority() == Priority.HIGH
                && request.estimatedValue().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Para prioridade HIGH, o valor estimado deve ser >= 100"
            );
        }
        solicitationMapper.updateStep3(request, solicitation);

        if (solicitation.getCurrentStep() < 3) {
            solicitation.setCurrentStep(3);
        }

        solicitation.setUpdatedAt(LocalDateTime.now());
        return solicitationRepository.save(solicitation);
    }


    public Solicitation submit(Integer id, User client) {
        Solicitation solicitation = findOwnedDraft(id, client);
        List<String> errors = validateCompleteness(solicitation);

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solicitação incompleta: " + String.join("; ", errors)
            );
        }

        solicitation.setStatus(SolicitationStatus.SUBMITTED);
        solicitation.setSubmittedAt(LocalDateTime.now());
        solicitation.setUpdatedAt(LocalDateTime.now());
        return solicitationRepository.save(solicitation);
    }


    public Solicitation findById(Integer id, User requester) {
        Solicitation solicitation = solicitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        if (!solicitation.getClientId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a essa solicitação");
        }
        return solicitation;
    }


    public List<Solicitation> list(User client, SolicitationStatus statusFilter) {
        if (statusFilter != null) {
            return solicitationRepository.findByClientIdAndStatus(client.getId(), statusFilter);
        }
        return solicitationRepository.findByClientId(client.getId());
    }


    private Solicitation findOwnedDraft(Integer id, User owner) {
        Solicitation solicitation = solicitationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        if (!solicitation.getClientId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a essa solicitação");
        }

        if (solicitation.getStatus() != SolicitationStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solicitação não está em rascunho");
        }
        return solicitation;
    }


    private List<String> validateCompleteness(Solicitation s) {
        List<String> errors = new ArrayList<>();

        if (s.getServiceType() == null) errors.add("ServiceType não preenchido");
        if (s.getTitle() == null || s.getTitle().isBlank()) errors.add("Title não preenchido");
        if (s.getDescription() == null || s.getDescription().isBlank()) errors.add("Description não preenchido");

        if (s.getCep() == null || s.getCep().isBlank()) errors.add("CEP não preenchido");
        if (s.getStreet() == null || s.getStreet().isBlank()) errors.add("Street não preenchido (CEP inválido?)");
        if (s.getNeighborhood() == null || s.getNeighborhood().isBlank()) errors.add("Neighborhood não preenchido");
        if (s.getCity() == null || s.getCity().isBlank()) errors.add("City não preenchido");
        if (s.getState() == null || s.getState().isBlank()) errors.add("State não preenchido");
        if (s.getNumber() == null || s.getNumber().isBlank()) errors.add("Number não preenchido");

        if (s.getPriority() == null) errors.add("Priority não preenchido");
        if (s.getPreferredDate() == null) errors.add("PreferredDate não preenchido");
        else if (s.getPreferredDate().isBefore(java.time.LocalDate.now())) errors.add("PreferredDate está no passado");
        if (s.getEstimatedValue() == null) errors.add("EstimatedValue não preenchido");
        if (s.getTermsAccepted() == null || !s.getTermsAccepted()) errors.add("TermsAccepted deve ser true");

        if (s.getPriority() == Priority.HIGH
                && s.getEstimatedValue() != null
                && s.getEstimatedValue().compareTo(BigDecimal.valueOf(100)) < 0) {
            errors.add("Para priority HIGH, estimatedValue deve ser >= 100");
        }
        return errors;
    }


}