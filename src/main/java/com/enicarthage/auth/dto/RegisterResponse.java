package com.enicarthage.auth.dto;

public class RegisterResponse {
    private final boolean success;
    private final String message;
    private final Integer idUtilisateur;

    public RegisterResponse(
            boolean success,
            String message,
            Integer idUtilisateur
    ) {
        this.success = success;
        this.message = message;
        this.idUtilisateur = idUtilisateur;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getIdUtilisateur() { return idUtilisateur; }
}