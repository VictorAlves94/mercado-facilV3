package com.mercadofacil.dto.request;

import com.mercadofacil.entity.LancamentoFiado;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LancamentoFiadoRequest(

    @NotNull(message = "Tipo de lançamento é obrigatório")
    LancamentoFiado.TipoLancamento tipo,

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor inválido")
    BigDecimal valor,

    @Size(max = 255, message = "Descrição pode ter no máximo 255 caracteres")
    String descricao
) {}
