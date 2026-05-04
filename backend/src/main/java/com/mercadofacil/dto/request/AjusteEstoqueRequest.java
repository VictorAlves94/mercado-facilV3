package com.mercadofacil.dto.request;

import com.mercadofacil.entity.MovimentacaoEstoque;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AjusteEstoqueRequest(

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    Integer quantidade,

    @NotNull(message = "Tipo de movimentação é obrigatório")
    MovimentacaoEstoque.TipoMovimentacao tipo,

    @NotBlank(message = "Motivo é obrigatório para ajuste de estoque")
    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    String motivo
) {}
