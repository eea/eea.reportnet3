#!/bin/sh -eu
if [ -z "${BACKEND:-}" ]; then
    BACKEND_JSON=undefined
else
    BACKEND_JSON=$(jq -n --arg backend $BACKEND '$backend')
fi
 
cat <<EOF
window.env = {
	  REACT_APP_BACKEND: $BACKEND_JSON;
};

EOF