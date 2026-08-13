MVNW := ./mvnw
COMPOSE := docker compose
API_URL := http://localhost:8080/api/v1/health

.PHONY: help test clean package run docker-build up-api down restart logs ps health

help:
	@echo "Comandos disponiveis:"
	@echo "  make test          - roda os testes"
	@echo "  make clean         - remove artefatos Maven"
	@echo "  make package       - gera o .jar da API"
	@echo "  make run           - roda a API localmente com profile dev"
	@echo "  make docker-build  - cria a imagem Docker"
	@echo "  make up-api        - sobe a API em container em background"
	@echo "  make down          - para e remove containers"
	@echo "  make restart       - reinicia a API em container"
	@echo "  make logs          - acompanha logs da API"
	@echo "  make ps            - lista containers do compose"
	@echo "  make health        - testa o endpoint de health"

test:
	$(MVNW) test

clean:
	$(MVNW) clean

package:
	$(MVNW) clean package

run:
	SPRING_PROFILES_ACTIVE=dev $(MVNW) spring-boot:run

docker-build:
	$(COMPOSE) build

up-api:
	$(COMPOSE) up -d --build

down:
	$(COMPOSE) down

restart: down up-api

logs:
	$(COMPOSE) logs -f api

ps:
	$(COMPOSE) ps

health:
	curl -s $(API_URL)
