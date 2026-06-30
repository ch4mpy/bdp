#!/usr/bin/env bash

source ./build-0-env.sh

# reverse-proxy
if [ "$CN" = "host.docker.internal" ] || [ "$CN" = "localhost" ] || [ "$CN" = `hostname` ] || [ "$CN" = "$HOSTNAME" ]; then
  echo "building reverse proxy image"
  docker build -t bdp-labs.c4-soft.com/reverse-proxy ./nginx-reverse-proxy
fi

# Keycloak
#
export SSL_PASSWORD=$(cat ./secrets/ssl/password.txt)
export KC_BOOTSTRAP_ADMIN_USERNAME=$(cat ./secrets/keycloak/admin_user.txt)
export KC_BOOTSTRAP_ADMIN_PASSWORD=$(cat ./secrets/keycloak/admin_password.txt)
export KC_DB_USERNAME=$(cat ./secrets/keycloak/postgres_user.txt)
export KC_DB_PASSWORD=$(cat ./secrets/keycloak/postgres_password.txt)

docker build \
  --build-arg KC_VERSION=$KC_VERSION \
  --build-arg CN=$CN \
  --build-arg KC_HEALTH_ENABLED=true \
  --build-arg KC_METRICS_ENABLED=true \
  --build-arg KC_DB=postgres \
  --build-arg KC_DB_HOST=$CN \
  --build-arg KC_DB_PORT=2632 \
  --build-arg KC_DB_NAME=keycloak \
  --build-arg KC_DB_SCHEMA=public \
  --build-arg KC_DB_URL=jdbc:postgresql://$KC_DB_HOST:$KC_DB_PORT/$KC_DB_NAME?currentSchema=$KC_DB_SCHEMA \
  --build-arg KC_HTTP_ENABLED=false \
  --build-arg KC_HTTPS_PORT=3643 \
  --build-arg KC_LOG_LEVEL=INFO \
  --secret id=ssl_password,src=./secrets/ssl/password.txt \
  -t bdp-labs.c4-soft.com/keycloak:$KC_VERSION \
  -f ./keycloak/Dockerfile \
  .

