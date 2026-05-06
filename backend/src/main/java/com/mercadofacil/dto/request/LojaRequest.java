package com.mercadofacil.dto.request;

import jakarta.validation.constraints.*;

public record LojaRequest(
        @NotBlank(message = "Nome da loja é obrigatório")
        @Size(min = 2, max = 100)
        String nome,

        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^[A-Z0-9\\-]{2,20}$",
                message = "Código deve ter letras maiúsculas, números e hífen")
        String codigo,

        String endereco,
        String telefone,
        String cnpj
){ }
