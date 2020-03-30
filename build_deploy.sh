#!/usr/bin/env bash


printf "\n==== LEIN CLEAN ====\n"
lein clean

printf "\n==== LEIN COMPILE ====\n"
lein with-profile webapps compile

lein with-profile gravsim uberjar
lein with-profile conway uberjar

printf "\n==== SCP TO HOME ====\n"
./deploy.sh