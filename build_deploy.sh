#!/usr/bin/env bash


printf "\n==== LEIN CLEAN ====\n"
lein clean

printf "\n==== LEIN COMPILE ====\n"
lein compile

printf "\n==== SCP TO HOME ====\n"
./deploy.sh