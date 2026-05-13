🏪 CaixaBsb G.F. — Gestão Financeira para Pequenos Mercados

Sistema completo de gestão para mercadinhos, distribuidoras e comércios locais.
Desenvolvido com Java 17 + Spring Boot + Angular.
Focado em controle de caixa, estoque e vendas de forma simples, rápida e moderna.

📋 Funcionalidades
Módulo	Funcionalidades
🔐 Autenticação	Login com JWT, perfis ADMIN / GERENTE / OPERADOR
📦 Produtos	CRUD completo, busca por nome/código/categoria
📊 Estoque	Controle automático, alertas de estoque baixo/zerado
🗓️ Validade	Controle de vencimento e alertas automáticos
🏪 Caixa	Abertura/fechamento de caixa e controle financeiro
🛍️ Vendas	Venda com múltiplos itens e baixa automática no estoque
💰 Financeiro	Controle de despesas, lucro diário e movimentações
📒 Fiado	Controle de clientes fiado (caderninho digital)
📈 Dashboard	Resumo diário de vendas, caixa, estoque e alertas
🛠️ Tecnologias
Back-end
Java 17
Spring Boot 3
Spring Security
Spring Data JPA
JWT Authentication
Maven
Lombok
PostgreSQL
H2 Database
Front-end
Angular 17+
TypeScript
Angular Material
Angular Router
Infraestrutura
Docker
Docker Compose
Profiles dev/prod
🚀 Como Rodar o Projeto
Opção 1 — Docker (Recomendado)
docker-compose up -d

Backend disponível em:

http://localhost:8080
Opção 2 — Rodar Localmente
Pré-requisitos
Java 17+
Maven 3.8+
Node.js 18+
PostgreSQL 14+
🔧 Back-end
Ambiente DEV (H2)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
Ambiente PROD (PostgreSQL)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=caixabsbdb
export DB_USER=postgres
export DB_PASS=123456

mvn spring-boot:run -Dspring-boot.run.profiles=prod
🎨 Front-end
cd frontend
npm install
ng serve

Acesse:

http://localhost:4200
🗄️ Banco de Dados
DEV — H2
Console H2:
http://localhost:8080/h2-console
JDBC URL:
jdbc:h2:mem:mercadofacildb

Usuário:

sa

Senha:

(vazio)
PROD — PostgreSQL
CREATE DATABASE caixabsbdb;

CREATE USER postgres WITH PASSWORD '123456';

GRANT ALL PRIVILEGES ON DATABASE caixabsbdb TO postgres;
🔑 Usuários de Teste
Usuário	Email	Senha	Perfil
Administrador	admin@caixabsb.com
	admin123	ADMIN
Operador	operador@caixabsb.com
	operador123	OPERADOR
📁 Estrutura do Projeto
mercadofacil/
├── backend/
│   ├── src/
│   │   ├── main/java/com/mercadofacil/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data-dev.sql
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
├── docker-compose.yml
└── README.md

⚠️ Observação: As pastas internas do projeto continuam utilizando o nome original mercadofacil para evitar problemas de compatibilidade e funcionamento da aplicação.

🔗 Principais Endpoints
POST /api/v1/auth/login
GET  /api/v1/auth/me

GET  /api/v1/dashboard/resumo

GET  /api/v1/produtos
POST /api/v1/produtos
PUT  /api/v1/produtos/{id}
DELETE /api/v1/produtos/{id}

POST /api/v1/caixa/abrir
POST /api/v1/caixa/fechar

POST /api/v1/vendas
GET  /api/v1/vendas

POST /api/v1/financeiro/despesas
GET  /api/v1/financeiro/resumo

GET  /api/v1/fiado
POST /api/v1/fiado
📈 Status do Desenvolvimento
 Sistema de autenticação JWT
 CRUD de produtos
 Controle de estoque
 Sistema de vendas
 Controle de caixa
 Controle financeiro
 Dashboard administrativo
 Front-end Angular funcional
🗺️ Próximas Melhorias
 Multi-loja
 Controle de permissões avançadas
 Auditoria de ações do sistema
 Relatórios PDF
 Integração Pix
 Impressora térmica
 Aplicativo mobile
 Backup automático
 Integração WhatsApp
📄 Licença

Sistema proprietário — CaixaBsb G.F. © 2026
Todos os direitos reservados.
