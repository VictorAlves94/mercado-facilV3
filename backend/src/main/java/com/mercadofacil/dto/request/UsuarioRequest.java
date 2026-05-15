package com.mercadofacil.dto.request;

import com.mercadofacil.entity.Usuario;
import jakarta.validation.constraints.*;

public record UsuarioRequest(

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    String nome,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,


    String senha,

    @NotNull(message = "Perfil é obrigatório")
    Usuario.Perfil perfil,

    Long lojaId

) {}
