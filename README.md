# 🛒 MercadoFácil — Sistema de Gestão para Pequenos Mercados

> Sistema completo de gestão para mercadinhos, mercearias e comércios locais.
> Desenvolvido em Java 17 + Spring Boot + Angular. Pronto para uso local ou SaaS.

---

## 📋 Funcionalidades

| Módulo | Funcionalidades |
|--------|----------------|
| 🔐 Autenticação | Login com JWT, perfis ADMIN / GERENTE / OPERADOR |
| 📦 Produtos | CRUD completo, busca por nome/código/categoria, código de barras |
| 📊 Estoque | Controle automático, alertas de estoque baixo/zerado, histórico |
| 🗓️ Validade | Alertas de vencimento (7 dias), produtos vencidos em destaque |
| 🏪 Caixa | Abrir/fechar caixa, saldo por forma de pagamento |
| 🛍️ Vendas | Venda com múltiplos itens, Dinheiro/Pix/Cartão, troco automático |
| 💰 Financeiro | Lançamento de despesas, lucro diário, saldo do caixa |
| 📒 Fiado | Caderninho digital de crédito por cliente (diferencial) |
| 📈 Dashboard | Resumo do dia: vendas, despesas, lucro, alertas |

---

## 🛠️ Tecnologias

**Back-end:**
- Java 17 · Spring Boot 3.2 · Spring Security · Spring Data JPA
- JWT (jjwt 0.12) · PostgreSQL · H2 (dev) · Maven · Lombok

**Front-end:**
- Angular 17+ · TypeScript · Angular Material · Angular Router

**Infra:**
- Docker · Docker Compose · Profiles (dev / prod)

---

## 🚀 Como Rodar

### Opção 1 — Docker (Recomendado)

```bash
# Na raiz do projeto
docker-compose up -d

# Backend disponível em: http://localhost:8080
```

### Opção 2 — Rodar localmente

#### Pré-requisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (ou use o perfil `dev` com H2)

#### Back-end (perfil dev com H2)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Back-end (perfil prod com PostgreSQL)
```bash
# Configure as variáveis de ambiente
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=mercadofacildb
export DB_USER=mercadofacil
export DB_PASS=mercadofacil123

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Front-end
```bash
cd frontend
npm install
ng serve
# Acesse: http://localhost:4200
```

---

## 🗄️ Banco de Dados

### Perfil DEV (H2 em memória)
- Console H2: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:mercadofacildb`
- Usuário: `sa` | Senha: (vazio)
- Dados de teste carregados automaticamente

### Perfil PROD (PostgreSQL)
```sql
CREATE DATABASE mercadofacildb;
CREATE USER mercadofacil WITH PASSWORD 'mercadofacil123';
GRANT ALL PRIVILEGES ON DATABASE mercadofacildb TO mercadofacil;
```

---

## 🔑 Usuários de Teste

| Usuário | Email | Senha | Perfil |
|---------|-------|-------|--------|
| Administrador | admin@mercadofacil.com | admin123 | ADMIN |
| Operador | operador@mercadofacil.com | operador123 | OPERADOR |

---

## 📁 Estrutura do Projeto

```
mercadofacil/
├── backend/
│   ├── src/
│   │   ├── main/java/com/mercadofacil/
│   │   │   ├── config/          # SecurityConfig, CORS
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/
│   │   │   │   ├── request/     # DTOs de entrada
│   │   │   │   └── response/    # DTOs de saída
│   │   │   ├── entity/          # Entidades JPA
│   │   │   ├── exception/       # Exceções customizadas + Handler global
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── security/        # JWT Service + Filter
│   │   │   └── service/         # Regras de negócio
│   │   └── resources/
│   │       ├── application.yml  # Config principal + perfis
│   │       └── data-dev.sql     # Dados de seed para desenvolvimento
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                    # Angular (Fase 5)
├── docker-compose.yml
└── README.md
```

---

## 🔗 Endpoints Principais

```
POST /api/v1/auth/login          # Login
GET  /api/v1/auth/me             # Dados do usuário logado

GET  /api/v1/dashboard/resumo    # Resumo do dia

GET  /api/v1/produtos            # Listar produtos (paginado)
POST /api/v1/produtos            # Criar produto
PUT  /api/v1/produtos/{id}       # Editar produto
DELETE /api/v1/produtos/{id}     # Desativar produto
GET  /api/v1/produtos/alertas    # Produtos com estoque baixo/vencidos

POST /api/v1/caixa/abrir         # Abrir caixa
POST /api/v1/caixa/fechar        # Fechar caixa
GET  /api/v1/caixa/atual         # Status do caixa atual

POST /api/v1/vendas              # Registrar venda
GET  /api/v1/vendas              # Histórico
POST /api/v1/vendas/{id}/cancelar # Cancelar venda

POST /api/v1/financeiro/despesas # Lançar despesa
GET  /api/v1/financeiro/resumo   # Lucro/despesas por período

GET  /api/v1/fiado               # Listar fiadeiros
POST /api/v1/fiado               # Criar fiado
POST /api/v1/fiado/{id}/pagar    # Registrar pagamento
```

---

## 📈 Fases do Desenvolvimento

- [x] **Fase 1** — Base: entidades, segurança JWT, repositórios, configuração
- [x] **Fase 2** — Módulo Produtos + Estoque + Usuários + Alertas automáticos
- [x] **Fase 3** — Módulo Vendas + Caixa + Relatórios
- [x] **Fase 4** — Módulo Financeiro + Fiado + Relatório P&L
- [x] **Fase 5** — Front-end Angular completo (todas as telas)

---

## 🗺️ Próximos Passos

- [ ] Emissão de Nota Fiscal (NF-e / NFC-e)
- [ ] Impressora térmica (ESC/POS)
- [ ] Integração Pix (Banco Central API)
- [ ] Resumo diário por WhatsApp (WhatsApp Business API)
- [ ] Modo offline (PWA)
- [ ] App mobile (Angular + Capacitor)
- [ ] Multi-empresa / Multi-filial
- [ ] Backup automático em nuvem
- [ ] Relatórios em PDF

---

## 📄 Licença

Proprietário — MercadoFácil © 2024. Todos os direitos reservados.
