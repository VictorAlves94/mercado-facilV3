package com.mercadofacil.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(

    String codigoBarras,

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres")
    String nome,

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    String descricao,

    Long categoriaId,

    @NotNull(message = "Quantidade em estoque é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    Integer quantidadeEstoque,

    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    Integer estoqueMinimo,

    @NotNull(message = "Preço de custo é obrigatório")
    @DecimalMin(value = "0.00", inclusive = false, message = "Preço de custo deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço de custo inválido")
    BigDecimal precoCusto,

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.00", inclusive = false, message = "Preço de venda deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço de venda inválido")
    BigDecimal precoVenda,

    @Future(message = "Data de validade deve ser uma data futura")
    LocalDate dataValidade
) {}
