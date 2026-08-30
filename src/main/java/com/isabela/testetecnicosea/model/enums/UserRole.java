package com.isabela.testetecnicosea.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(enumAsRef = true, description = "Perfil de acesso dos usuários")
public enum UserRole {
    CLIENT("Cliente"),
    ANALYST("Analista"),
    ADMIN("Administrador");

    private final String role;
    UserRole(String role) { this.role = role; }

    public String getRole() {
        return role;
    }

}
