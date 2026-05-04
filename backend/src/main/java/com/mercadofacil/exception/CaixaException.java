package com.mercadofacil.exception;

public class CaixaException extends RuntimeException {
    public CaixaException(String message) { super(message); }

    public static CaixaException semCaixaAberto() {
        return new CaixaException("Não há caixa aberto. Abra o caixa antes de registrar vendas.");
    }

    public static CaixaException caixaJaAberto() {
        return new CaixaException("Já existe um caixa aberto. Feche o caixa atual antes de abrir um novo.");
    }
}
