# Auth Service

Authentication Service using [Keycloak](https://www.keycloak.org/) as OAuth2 Resource Server

## Local Setup

* Setup Keycloak https://www.keycloak.org/guides
  * Docker compose sample is also available [here](https://github.com/mcboyao/docker/tree/main/keycloak)
* Configure required environment variables
````
KEYCLOAK_URI=<uri e.g. http://localhost:8180>
KEYCLOAK_REALM=<realm-name>
KEYCLOAK_CLIENT_ID=<keycloak client-id>
KEYCLOAK_CLIENT_SECRET=<keycloak client-id secret>
````
* Start the application
* Access API at http://localhost:8080/swagger-ui/index.html

## Docker

* Build image `docker build --no-cache -t backend-auth .`
* Run `docker run -e KEYCLOAK_URI=http://localhost:8180 -e KEYCLOAK_REALM=realm-name -e KEYCLOAK_CLIENT_ID=client_id -e KEYCLOAK_CLIENT_SECRET=client_secret -p 8080:8080 -d backend-auth:latest`