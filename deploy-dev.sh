#!/usr/bin/env bash

sdk env install
nvm install --lts
nvm use
cd api && mvn install -Popenapi,h2 && cd ..
git submodule init && git submodule update
cd frontend && npm i && npm run api && cd ..

source ./build-1-images.sh

export KC_BOOTSTRAP_ADMIN_USERNAME=$(cat ./secrets/keycloak/admin_user.txt)
export KC_BOOTSTRAP_ADMIN_PASSWORD=$(cat ./secrets/keycloak/admin_password.txt)
export KC_DB_USERNAME=$(cat ./secrets/keycloak/postgres_user.txt)
export KC_DB_PASSWORD=$(cat ./secrets/keycloak/postgres_password.txt)

docker compose \
  -f compose-rest-api-db.yml \
  -f compose-mailpit.yml \
  -f compose-keycloak.yml \
  -f compose-observability.yml \
  -f compose-reverse-proxy.yml \
  up -d