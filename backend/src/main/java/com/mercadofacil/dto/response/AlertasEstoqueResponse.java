package com.mercadofacil.dto.response;

import java.util.List;

public record AlertasEstoqueResponse(
    List<ProdutoResponse> estoqueBaixo,
    List<ProdutoResponse> estoqueZerado,
    List<ProdutoResponse> validadeProxima,
    List<ProdutoResponse> vencidos,
    long totalAlertas
) {
    public static AlertasEstoqueResponse of(
            List<ProdutoResponse> baixo,
            List<ProdutoResponse> zerado,
            List<ProdutoResponse> validade,
            List<ProdutoResponse> vencidos) {
        long total = baixo.size() + zerado.size() + validade.size() + vencidos.size();
        return new AlertasEstoqueResponse(baixo, zerado, validade, vencidos, total);
    }
}
