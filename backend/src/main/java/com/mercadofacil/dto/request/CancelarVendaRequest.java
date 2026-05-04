package com.mercadofacil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarVendaRequest(

    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    @Size(min = 5, max = 255, message = "Motivo deve ter entre 5 e 255 caracteres")
    String motivo
) {}
