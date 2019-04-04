#!/bin/bash
service filebeat start
echo "$*"
/bin/sh -c "$*"