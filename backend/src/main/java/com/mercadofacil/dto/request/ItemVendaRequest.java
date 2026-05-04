package com.mercadofacil.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ItemVendaRequest(

    @NotNull(message = "ID do produto é obrigatório")
    Long produtoId,

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    Integer quantidade,

    @DecimalMin(value = "0.00", message = "Desconto não pode ser negativo")
    @DecimalMax(value = "100.00", message = "Desconto não pode ultrapassar 100%")
    @Digits(integer = 3, fraction = 2, message = "Desconto inválido")
    BigDecimal desconto
) {
    // desconto padrão zero se não informado
    public BigDecimal desconto() {
        return desconto != null ? desconto : BigDecimal.ZERO;
    }
}
