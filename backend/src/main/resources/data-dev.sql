-- ================================================
-- CaixaBSB — Dados iniciais para desenvolvimento
-- ================================================

-- Usuários (senha: admin123 — bcrypt)
INSERT INTO usuarios (nome, email, senha, perfil, ativo, criado_em)
VALUES ('Administrador', 'admin@caixabsb.com',
        '$2a$10$yzNyEjWtXZfB6usWgat7Nu2Cr2VspDJm8QWGZs2Cm3q0BW.QKtifW',
        'ADMIN', true, CURRENT_TIMESTAMP);

INSERT INTO usuarios (nome, email, senha, perfil, ativo, criado_em)
VALUES ('João Operador', 'operador@caixabsb.com',
        '$2a$10$yzNyEjWtXZfB6usWgat7Nu2Cr2VspDJm8QWGZs2Cm3q0BW.QKtifW',
        'OPERADOR', true, CURRENT_TIMESTAMP);

-- Categorias
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Bebidas',    'Águas, sucos, refrigerantes e cervejas', true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Laticínios', 'Leite, queijo, iogurte e derivados',     true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Padaria',    'Pães, bolos e biscoitos',                true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Hortifrúti', 'Frutas, legumes e verduras',             true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Limpeza',    'Produtos de limpeza doméstica',          true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Higiene',    'Produtos de higiene pessoal',            true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Mercearia',  'Arroz, feijão, açúcar e massas',        true);
INSERT INTO categorias (nome, descricao, ativo) VALUES ('Frios',      'Frios e embutidos',                      true);

-- Lojas
INSERT INTO lojas (nome, codigo, endereco, telefone, cnpj, ativa, criado_em)
VALUES ('Loja Centro', 'CENTRO', 'Quadra 1, Bloco A, Loja 10 - Brasília/DF',
        '(61) 3333-1111', '11.222.333/0001-44', true, CURRENT_TIMESTAMP);

INSERT INTO lojas (nome, codigo, endereco, telefone, cnpj, ativa, criado_em)
VALUES ('Loja Norte', 'NORTE', 'Quadra 412 Norte, Bloco C, Loja 5 - Brasília/DF',
        '(61) 3333-2222', '11.222.333/0002-25', true, CURRENT_TIMESTAMP);

-- Gerente vinculado à Loja 1
INSERT INTO usuarios (nome, email, senha, perfil, ativo, loja_id, criado_em)
VALUES ('Maria Gerente', 'gerente@caixabsb.com',
        '$2a$10$yzNyEjWtXZfB6usWgat7Nu2Cr2VspDJm8QWGZs2Cm3q0BW.QKtifW',
        'GERENTE', true, 1, CURRENT_TIMESTAMP);

-- Vincular operador à Loja 1
UPDATE usuarios SET loja_id = 1 WHERE email = 'operador@caixabsb.com';

-- Produtos
INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7891000100103', 'Coca-Cola 2L', 'Refrigerante Coca-Cola 2 litros', 1, 48, 10, 6.50, 9.99, '2025-12-31', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7891000315507', 'Leite Integral 1L', 'Leite Integral Longa Vida', 2, 60, 20, 3.20, 5.49, '2025-06-30', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7896024301004', 'Arroz Tipo 1 5kg', 'Arroz Branco Tipo 1', 7, 30, 10, 18.00, 27.90, '2026-06-01', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7896024200014', 'Feijão Carioca 1kg', 'Feijão Carioca Tipo 1', 7, 25, 10, 7.50, 12.90, '2026-01-01', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7891910000197', 'Açúcar Cristal 1kg', 'Açúcar Cristal Refinado', 7, 8, 10, 3.80, 6.49, '2026-12-31', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7896036090039', 'Macarrão Espaguete 500g', 'Macarrão Espaguete Grano Duro', 7, 5, 10, 3.00, 5.49, '2026-08-01', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7891149101704', 'Sabão em Pó 1kg', 'Detergente em Pó para Roupas', 5, 0, 10, 8.50, 14.99, '2027-01-01', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7891007270016', 'Mortadela Fatiada 200g', 'Mortadela Tradicional Fatiada', 8, 15, 5, 4.20, 7.99, '2025-04-15', true, CURRENT_TIMESTAMP);

INSERT INTO produtos (codigo_barras, nome, descricao, categoria_id, quantidade_estoque, estoque_minimo, preco_custo, preco_venda, data_validade, ativo, criado_em)
VALUES ('7891000248058', 'Iogurte Natural 170g', 'Iogurte Natural Integral', 2, 12, 10, 2.10, 4.29, '2025-04-10', true, CURRENT_TIMESTAMP);

-- Tipos de despesa
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Água',              'Conta de água',               true);
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Energia Elétrica',  'Conta de luz',                true);
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Funcionário',       'Salário de funcionários',     true);
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Fornecedor',        'Pagamento a fornecedores',    true);
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Aluguel',           'Aluguel do estabelecimento',  true);
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Manutenção',        'Reparos e manutenção',        true);
INSERT INTO tipos_despesa (nome, descricao, ativo) VALUES ('Outros',            'Outras despesas',             true);

-- Auditoria de exemplo
INSERT INTO audit_log (usuario_nome, acao, descricao, entidade, entidade_referencia, criado_em)
VALUES ('João Operador', 'VENDA_CANCELADA',
        'João Operador cancelou a venda V20250601002 - Motivo: Produto errado',
        'Venda', 'V20250601002', CURRENT_TIMESTAMP - INTERVAL '1' HOUR);

INSERT INTO audit_log (usuario_nome, acao, descricao, entidade, entidade_referencia, valor_anterior, valor_posterior, criado_em)
VALUES ('Administrador', 'ESTOQUE_AJUSTE',
        'Administrador alterou estoque de Arroz 5kg de 30 para 20 unidades - Inventário',
        'Produto', 'Arroz 5kg', '30', '20', CURRENT_TIMESTAMP - INTERVAL '3' HOUR);

INSERT INTO audit_log (usuario_nome, acao, descricao, entidade, criado_em)
VALUES ('Administrador', 'CAIXA_ABERTO',
        'Administrador abriu o caixa #1 - Fundo: R$ 100.00',
        'Caixa', CURRENT_TIMESTAMP - INTERVAL '8' HOUR);