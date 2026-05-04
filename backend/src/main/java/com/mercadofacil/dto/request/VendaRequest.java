package com.mercadofacil.dto.request;

import com.mercadofacil.entity.Venda;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record VendaRequest(

    @NotNull(message = "Forma de pagamento é obrigatória")
    Venda.FormaPagamento formaPagamento,

    @NotNull(message = "A venda deve ter pelo menos um item")
    @Size(min = 1, message = "A venda deve ter pelo menos um item")
    @Valid
    List<ItemVendaRequest> itens,

    @DecimalMin(value = "0.00", message = "Desconto não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Desconto geral inválido")
    BigDecimal descontoGeral,

    // Valor pago pelo cliente (obrigatório para DINHEIRO, opcional para demais)
    @DecimalMin(value = "0.00", message = "Valor recebido não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Valor recebido inválido")
    BigDecimal valorRecebido
) {
    public BigDecimal descontoGeral() {
        return descontoGeral != null ? descontoGeral : BigDecimal.ZERO;
    }
}
