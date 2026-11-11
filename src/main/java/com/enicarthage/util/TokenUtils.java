package com.enicarthage.util;

import com.enicarthage.exception.AuthenticationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TokenUtils {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long EXPIRATION_SECONDS = 8 * 3600; // 8 hours
    private static final String DEFAULT_DEV_SECRET = "AlZahrawi_MedicalCabinet_GestCabMed_SecureSecretKey_2026_Minimum32Chars";

    private static String getSecret() {
        String envSecret = System.getenv("APP_JWT_SECRET");
        if (envSecret != null && envSecret.trim().length() >= 32) {
            return envSecret.trim();
        }
        return DEFAULT_DEV_SECRET;
    }

    public static class UserPrincipal {
        private final int idUtilisateur;
        private final String role;

        public UserPrincipal(int idUtilisateur, String role) {
            this.idUtilisateur = idUtilisateur;
            this.role = role;
        }

        public int getIdUtilisateur() {
            return idUtilisateur;
        }

        public String getRole() {
            return role;
        }
    }

    /**
     * Generates a signed Bearer token (HMAC-SHA256) valid for 8 hours.
     */
    public static String generateToken(int idUtilisateur, String role) {
        long iat = System.currentTimeMillis() / 1000L;
        long exp = iat + EXPIRATION_SECONDS;
        String rawPayload = idUtilisateur + ":" + role + ":" + iat + ":" + exp;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(rawPayload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload, getSecret());
        return encodedPayload + "." + signature;
    }

    /**
     * Verifies the token using constant-time comparison and checks expiration.
     */
    public static UserPrincipal verifyToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Jeton d'authentification manquant.");
        }

        String[] parts = token.trim().split("\\.");
        if (parts.length != 2) {
            throw new AuthenticationException("Format de jeton invalide.");
        }

        String encodedPayload = parts[0];
        String receivedSignature = parts[1];
        String expectedSignature = sign(encodedPayload, getSecret());

        // Constant-time signature comparison to prevent timing attacks
        byte[] expectedBytes = expectedSignature.getBytes(StandardCharsets.UTF_8);
        byte[] receivedBytes = receivedSignature.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expectedBytes, receivedBytes)) {
            throw new AuthenticationException("Signature du jeton invalide ou corrompue.");
        }

        String rawPayload;
        try {
            rawPayload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Données du jeton illisibles.");
        }

        String[] payloadParts = rawPayload.split(":");
        if (payloadParts.length < 4) {
            throw new AuthenticationException("Structure de jeton invalide.");
        }

        try {
            int id = Integer.parseInt(payloadParts[0]);
            String role = payloadParts[1];
            long exp = Long.parseLong(payloadParts[3]);

            long now = System.currentTimeMillis() / 1000L;
            if (now > exp) {
                throw new AuthenticationException("Session expirée (au-delà de 8 heures). Veuillez vous reconnecter.");
            }

            return new UserPrincipal(id, role);
        } catch (NumberFormatException e) {
            throw new AuthenticationException("Champs numériques du jeton invalides.");
        }
    }

    private static String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Erreur de calcul HMAC", e);
        }
    }
}
