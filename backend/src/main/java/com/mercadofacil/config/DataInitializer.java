package com.mercadofacil.config;

import com.mercadofacil.entity.Usuario;
import com.mercadofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (usuarioRepository.findByEmail("admin@caixabsb.com").isEmpty()) {

            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email("admin@caixabsb.com")
                    .senha(passwordEncoder.encode("123"))
                    .perfil(Usuario.Perfil.ADMIN)
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);

            System.out.println("ADMIN PADRÃO CRIADO.");
        }
    }
}