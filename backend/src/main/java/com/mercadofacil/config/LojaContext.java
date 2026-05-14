package com.mercadofacil.config;

/**
 * Armazena o lojaId da requisição atual via ThreadLocal.
 * Thread-safe — cada thread (request) tem valor isolado.
 * LojaFilter chama clear() no finally de cada request.
 */
public class LojaContext {
    private static final ThreadLocal<Long> LOJA_ID = new ThreadLocal<>();

    private LojaContext() {}

    public static void set(Long lojaId) { LOJA_ID.set(lojaId);  }
    public static Long  get()           { return LOJA_ID.get(); }
    public static void  clear()         { LOJA_ID.remove();     }
}
