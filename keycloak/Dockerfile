ARG KC_VERSION=26.6
ARG CN=host.docker.internal
ARG KC_HEALTH_ENABLED=true
ARG KC_METRICS_ENABLED=true
ARG KC_DB=postgres
ARG KC_DB_HOST=host.docker.internal
ARG KC_DB_PORT=2632
ARG KC_DB_NAME=keycloak
ARG KC_DB_SCHEMA=public
ARG KC_DB_URL=jdbc:postgresql://${KC_DB_HOST}:${KC_DB_PORT}/${KC_DB_NAME}?currentSchema=${KC_DB_SCHEMA}
ARG KC_HTTP_ENABLED=false
ARG KC_HTTPS_PORT=3643
ARG KC_LOG_LEVEL=INFO

FROM quay.io/keycloak/keycloak:${KC_VERSION}

ARG CN
ARG KC_HOSTNAME
ARG KC_HEALTH_ENABLED
ARG KC_METRICS_ENABLED
ARG KC_DB
ARG KC_DB_HOST
ARG KC_DB_PORT
ARG KC_DB_NAME
ARG KC_DB_SCHEMA
ARG KC_DB_URL
ARG KC_HTTP_ENABLED
ARG KC_HTTPS_PORT
ARG KC_LOG_LEVEL

ENV KC_HTTP_RELATIVE_PATH=/auth/
ENV KC_HOSTNAME=https://${CN}/auth/
ENV KC_HOSTNAME_ADMIN=https://${CN}/auth/
ENV KC_HEALTH_ENABLED=$KC_HEALTH_ENABLED
ENV KC_METRICS_ENABLED=$KC_METRICS_ENABLED
ENV KC_DB=$KC_DB
ENV KC_DB_HOST=$KC_DB_HOST
ENV KC_DB_PORT=$KC_DB_PORT
ENV KC_DB_NAME=$KC_DB_NAME
ENV KC_DB_SCHEMA=$KC_DB_SCHEMA
ENV KC_DB_URL=$KC_DB_URL
ENV KC_HTTP_ENABLED=$KC_HTTP_ENABLED
ENV KC_HTTPS_PORT=$KC_HTTPS_PORT
ENV KC_LOG_LEVEL=$KC_LOG_LEVEL

ENV TZ="Pacific/Tahiti"

USER root 

COPY --chown=keycloak:keycloak ./certs/${CN}.jks /tmp/${CN}.jks
COPY --chown=keycloak:keycloak ./certs/${CN}.crt /opt/keycloak/certs/${CN}.crt
COPY --chown=keycloak:keycloak ./certs/${CN}.key /opt/keycloak/certs/${CN}.key
COPY --chown=keycloak:keycloak ./keycloak/themes/ /opt/keycloak/themes/

RUN --mount=type=secret,id=ssl_password,env=SSL_PASSWORD \
    keytool -importkeystore -srckeystore /tmp/${CN}.jks -srckeypass ${SSL_PASSWORD} -srcstorepass ${SSL_PASSWORD} -srcstoretype pkcs12 -srcalias ${CN} -cacerts -deststorepass changeit -destalias ${CN}

RUN rm -f /tmp/${CN}.jks

USER keycloak

RUN /opt/keycloak/bin/kc.sh build