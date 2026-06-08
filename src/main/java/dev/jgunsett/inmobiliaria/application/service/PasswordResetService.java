package dev.jgunsett.inmobiliaria.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.auth.ForgotPasswordRequest;
import dev.jgunsett.inmobiliaria.application.dto.auth.ResetPasswordRequest;
import dev.jgunsett.inmobiliaria.domain.entity.PasswordResetToken;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.repository.PasswordResetTokenRepository;
import dev.jgunsett.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Inicia el flujo de recuperación de contraseña.
     * Siempre responde con éxito para no revelar si un email existe.
     */
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            log.info("Recuperación solicitada para email no registrado: {}", request.getEmail());
            return;
        }

        User user = userOpt.get();

        // Eliminar tokens anteriores del mismo usuario
        passwordResetTokenRepository.deleteByUser(user);

        // Crear nuevo token con expiración
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Enviar email
        String resetLink = frontendUrl + "/reset-password?token=" + tokenValue;
        String body = """
                Recibiste este email porque solicitaste restablecer tu contraseña.

                Hacé click en el siguiente enlace para crear una nueva contraseña:

                %s

                Este enlace expira en %d minutos.

                Si no solicitaste este cambio, podés ignorar este email.
                Tu contraseña no será modificada.
                """.formatted(resetLink, TOKEN_EXPIRY_MINUTES);

        emailSenderService.sendNotificationEmail(
                user.getEmail(),
                "Restablecer contraseña — Inmobiliaria",
                body
        );

        log.info("Token de recuperación generado para: {}", user.getEmail());
    }

    /**
     * Valida el token y actualiza la contraseña.
     *
     * @throws IllegalArgumentException si el token no existe, ya fue usado o expiró
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o inexistente"));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new IllegalArgumentException("Este enlace ya fue utilizado. Solicitá uno nuevo si necesitás cambiar tu contraseña");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("El enlace expiró. Solicitá uno nuevo para continuar");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Contraseña restablecida exitosamente para: {}", user.getEmail());
    }
}
