CERTIFICATE=$1
if [ ! -f "${CERTIFICATE}" ]; then
  echo "Usage: add-to-jre-cacerts.sh CERTIFICATE SSL_PASSWORD SRC_ALIAS DEST_ALIAS"
  echo "The certificate CERTIFICATE is mandatory and must exist."
  echo "SSL_PASSWORD is the password for the source key and keystore. It defaults to \"secret\""
  echo "SRC_ALIAS is the alias for the certificate in the source keystore. It defaults to \"host.docker.internal\""
  echo "DEST_ALIAS is the alias for the certificate in the cacerts file. It defaults to \"tickee\""
  exit 1
fi

SSL_PASSWORD=$2
if [ -z "${SSL_PASSWORD}" ]; then
  SSL_PASSWORD=$SERVER_SSL_KEY_PASSWORD
fi
if [ -z "${SSL_PASSWORD}" ]; then
  SSL_PASSWORD=secret
fi

SRC_ALIAS=$3
if [ -z "${SRC_ALIAS}" ]; then
  SRC_ALIAS=host.docker.internal
fi

DEST_ALIAS=$4
if [ -z "${DEST_ALIAS}" ]; then
  DEST_ALIAS=${SRC_ALIAS}
fi

CACERTS_PWD="changeit"

read -p "JAVA_HOME (default: ${JAVA_HOME}): " -r JAVA
JAVA=${JAVA:-${JAVA_HOME}}
JAVA=$(echo "$JAVA" | sed 's/\\/\//g')
if [ -z "${JAVA}" ]; then
  echo "ERROR: could not locate JDK / JRE root directory"
  exit 1
fi
# Locate cacerts file
if [ -f "${JAVA}/lib/security/cacerts" ]; then
  # recent JDKs and JREs style
  CACERTS=("${JAVA}/lib/security/cacerts")
elif [ -f "${JAVA}/jre/lib/security/cacerts" ]; then
  # legacy JDKs style (1.8 and older)
  CACERTS=("${JAVA}/jre/lib/security/cacerts")
else
  echo "ERROR: could not locate cacerts under $JAVA"
  exit 1
fi

read -p "cacerts password (default: ${CACERTS_PWD}): " CACERTS_PASSWORD
CACERTS_PASSWORD=${CACERTS_PASSWORD:-${CACERTS_PWD}}

echo "Importing certificate into cacerts..."
"${JAVA}/bin/keytool" -importkeystore -srckeystore "${CERTIFICATE}" -srckeypass "${SSL_PASSWORD}" -srcstorepass "${SSL_PASSWORD}" -srcstoretype pkcs12 -srcalias "${SRC_ALIAS}" -destkeystore "${CACERTS}" -deststorepass "${CACERTS_PASSWORD}" -destalias "${DEST_ALIAS}"