package com.mercadofacil.config;

import com.mercadofacil.entity.Loja;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.repository.LojaRepository;
import com.mercadofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LojaRepository    lojaRepository;       // ← ADICIONAR
    private final PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) {

        // ── Lojas padrão ──────────────────────────────────────────
        if (lojaRepository.count() == 0) {
            lojaRepository.save(Loja.builder()
                    .nome("Loja 1")
                    .codigo("BSB-01")
                    .ativa(true)
                    .build());

            lojaRepository.save(Loja.builder()
                    .nome("Loja 2")
                    .codigo("BSB-02")
                    .ativa(true)
                    .build());

            System.out.println("LOJAS PADRÃO CRIADAS.");
        }

        // ── Admin padrão ──────────────────────────────────────────
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