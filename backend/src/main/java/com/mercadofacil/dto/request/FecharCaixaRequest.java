package com.mercadofacil.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record FecharCaixaRequest(

    @NotNull(message = "Valor de fechamento é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor de fechamento não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Valor de fechamento inválido")
    BigDecimal valorFechamento,

    @Size(max = 500, message = "Observação pode ter no máximo 500 caracteres")
    String observacao
) {}
