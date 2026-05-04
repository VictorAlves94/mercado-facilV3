package com.mercadofacil.dto.request;

import com.mercadofacil.entity.Despesa;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaRequest(

    @NotNull(message = "Tipo de despesa é obrigatório")
    Long tipoDespesaId,

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 255, message = "Descrição deve ter entre 3 e 255 caracteres")
    String descricao,

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor inválido")
    BigDecimal valor,

    @NotNull(message = "Data da despesa é obrigatória")
    LocalDate dataDespesa,

    Despesa.FormaPagamentoDespesa formaPagamento,

    @Size(max = 500, message = "Observação pode ter no máximo 500 caracteres")
    String observacao
) {}
