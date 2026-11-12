package com.enicarthage.model;

import com.enicarthage.dto.AuthResponseDTO;

public class AuthResponse extends AuthResponseDTO {

    public AuthResponse(boolean success, String message, int idUtilisateur,
                        String nom, String prenom, String login, String role) {
        super(success, message, idUtilisateur, nom, prenom, login, role);
    }
}
