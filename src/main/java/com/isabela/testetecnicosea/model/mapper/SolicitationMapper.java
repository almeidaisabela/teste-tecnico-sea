package com.isabela.testetecnicosea.model.mapper;

import com.isabela.testetecnicosea.model.dto.SolicitationResponseDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep1RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep2RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep3RequestDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;


@Mapper(componentModel = "spring")
public interface SolicitationMapper {

    default Solicitation toNewEntity(Integer clientId) {
        Solicitation solicitation = new Solicitation();
        solicitation.setClientId(clientId);
        solicitation.setStatus(SolicitationStatus.DRAFT);
        solicitation.setCurrentStep(0);
        solicitation.setCreatedAt(LocalDateTime.now());
        return solicitation;
    }

    void updateStep1(SolicitationStep1RequestDTO request, @MappingTarget Solicitation solicitation);

    void updateStep2(SolicitationStep2RequestDTO request, @MappingTarget Solicitation solicitation);

    void updateStep3(SolicitationStep3RequestDTO request, @MappingTarget Solicitation solicitation);

    SolicitationResponseDTO toResponse(Solicitation solicitation);

}
