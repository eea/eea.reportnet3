#!/bin/sh -eu
if [ -z "${BACKEND:-}" ]; then
    BACKEND_JSON=undefined
else
    BACKEND_JSON=$(jq -n --arg backend $BACKEND '$backend')
fi

if [ -z "${EULOGIN:-}" ]; then
    EULOGIN_JSON=undefined
else
    EULOGIN_JSON=$(jq -n --arg eulogin $EULOGIN '$eulogin')
fi

if [ -z "${KEYCLOAK:-}" ]; then
    KEYCLOAK_JSON=undefined
else
    KEYCLOAK_JSON=$(jq -n --arg keycloak $KEYCLOAK '$keycloak')
fi
 
if [ -z "${WEBSOCKET_URL:-}" ]; then
    WEBSOCKET_URL_JSON=undefined
else
    WEBSOCKET_URL_JSON=$(jq -n --arg websocketUrl $WEBSOCKET_URL '$websocketUrl')
fi

if [ -z "${DOCUMENT_FOLDER:-}" ]; then
    DOCUMENT_FOLDER_JSON=undefined
else
    DOCUMENT_FOLDER_JSON=$(jq -n --arg documentFolder DOCUMENT_FOLDER '$documentFolder')
fi

cat <<EOF
window.env = {
	  REACT_APP_BACKEND: $BACKEND_JSON,
	  EULOGIN_URL: $EULOGIN_JSON,
	  REACT_APP_EULOGIN: $KEYCLOAK_JSON,
	  WEBSOCKET_URL: $WEBSOCKET_URL_JSON,
	  DOCUMENT_FOLDER: $DOCUMENT_FOLDER_JSON
};

EOF