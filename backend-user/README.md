# User Service

User Service using [Keycloak](https://www.keycloak.org/) as OAuth2 Resource Server, [PostgreSQL](https://www.postgresql.org/) database to store other user information and [Vault](https://developer.hashicorp.com/vault/docs) to manage secrets.

## Local Setup

* Setup Keycloak https://www.keycloak.org/guides
  * Docker compose sample is also available [here](https://github.com/mcboyao/docker/tree/main/keycloak)
* Setup PostgreSQL database https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/
  * Docker compose sample is also available [here](https://github.com/mcboyao/docker/tree/main/postgres)
* Setup Vault https://developer.hashicorp.com/vault/docs/install
  * Docker compose sample is also available [here](https://github.com/mcboyao/docker/tree/main/vault)
  * Configure PostgreSQL secrets via Admin Console or CLI
````
vault kv put secret/backend-user PG_USERNAME=postgres PG_PASSWORD=postgres PG_HOST=localhost PG_PORT=5432 PG_DATABASE=postgres PG_SCHEMA=public
````
* Configure required environment variables
````
VAULT_TOKEN=token
KEYCLOAK_URI=<uri e.g. http://localhost:8180>
KEYCLOAK_REALM=<realm-name>
KEYCLOAK_CLIENT_ID=<keycloak client-id>
KEYCLOAK_CLIENT_SECRET=<keycloak client-id secret>
````
* Start the application
* Access API at http://localhost:8081/swagger-ui/index.html

## Docker

* Build image `docker build --no-cache -t backend-user .`
* Run `docker run -e VAULT_TOKEN=token -e PG_HOST=localhost -e PG_PORT=5432 -e PG_DATABASE=postgres -e PG_SCHEMA=public -e PG_USERNAME=postgres -e PG_PASSWORD=postgres -e KEYCLOAK_URI=http://localhost:8180 -e KEYCLOAK_REALM=realm-name -e KEYCLOAK_CLIENT_ID=client_id -e KEYCLOAK_CLIENT_SECRET=client_secret -p 8081:8091 -d backend-user:latest`