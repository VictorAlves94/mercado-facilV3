package com.mercadofacil.exception;

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(String nomeProduto, int disponivel, int solicitado) {
        super(String.format("Estoque insuficiente para '%s'. Disponível: %d, Solicitado: %d",
                nomeProduto, disponivel, solicitado));
    }
}
