package com.mercadofacil.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AbrirCaixaRequest(

    @NotNull(message = "Valor de abertura é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor de abertura não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Valor de abertura inválido")
    BigDecimal valorAbertura
) {}
