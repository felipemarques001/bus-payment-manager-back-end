# 🚌 BusPaymentManager - API

A API do BusPaymentManager é uma aplicação Java e Spring que tem como objetivo gerenciar as mensalidades dos alunos universitários que utilizam um ônibus particular, oferecendo uma experiência mais simples e rápida, facilitando o controle de tais mensalidades. 

## ✨ Funcionalidades
- CRUD de alunos, pagamentos e mensalidades.
- Resultados de buscaso enviados em formato de paginações.
- Autenticação com base em token de acesso JWT.
- Geração de cookie HTTP only que armazena um refresh token usado para atualização de token de acesso sem necessitar autenticação repetidamente do usuário.
- Cobertura de 97% do código com testes automatizados unitários e de integração.
- Armazenameto dos dados em banco SQL PostgreSQL
- Uso de migrations para controle e versionamento do SQL que gera o banco de dados.
- Uso de padrão de projeto simples que facilita a manutenção e melhoria do código. 

## 🛠 Tecnologias e Biblotecas Utilizadas - Back-end
- Java 21
- Ecossitemas Spring (Spring Boot, Spring Data JPA, Spring Security)
- PostgreSQL
- H2
- Tokens e Cookies JWT
- Flyway
- Lombok
- JUnit 5
- Mockito
- Swagger
- Git

## 📋 Documentação da API
- A documentação da API foi gerada automaticamente pelo Swagger. Você pode acessá-la através do endpoint "**/swagger-ui/index.html**".
  - Exemplo: http://localhost:8080/swagger-ui/index.html
  - **Observação:** O acesso à documentação está disponível somente enquanto a API estiver em execução localmente.
  
## Link para o repositório do Front-End:
- https://github.com/felipemarques001/bus-payment-manager-frontend
