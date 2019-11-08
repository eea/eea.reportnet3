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
 
cat <<EOF
window.env = {
	  REACT_APP_BACKEND: $BACKEND_JSON,
	  EULOGIN_URL: $EULOGIN_JSON,
	  REACT_APP_EULOGIN: $KEYCLOAK_JSON
};

EOF