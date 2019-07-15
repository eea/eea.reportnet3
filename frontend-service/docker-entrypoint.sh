#!/bin/sh -eu
./generate_config_js.sh >/usr/share/nginx/html/env.js
nginx -g "daemon off;"