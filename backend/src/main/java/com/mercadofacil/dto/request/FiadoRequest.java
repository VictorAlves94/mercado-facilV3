package com.mercadofacil.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FiadoRequest(

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres")
    String nomeCliente,

    @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
    String telefoneCliente,

    @DecimalMin(value = "0.00", message = "Limite de crédito não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Limite de crédito inválido")
    BigDecimal limiteCredito
) {}
