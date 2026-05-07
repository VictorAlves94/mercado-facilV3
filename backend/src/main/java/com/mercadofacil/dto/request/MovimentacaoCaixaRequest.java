package com.mercadofacil.dto.request;

import com.mercadofacil.entity.MovimentacaoCaixa;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public record MovimentacaoCaixaRequest(

        @NotNull(message = "Tipo é obrigatório")
        MovimentacaoCaixa.TipoMovimentacaoCaixa tipo,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2)
        BigDecimal valor,

        @NotBlank(message = "Motivo é obrigatório")
        @Size(min = 3, max = 255)
        String motivo

) {
}
