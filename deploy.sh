#!/usr/bin/env bash

scp -P 122 resources/*.html finkmar@homedir.tngtech.com:public_html
scp -P 122 resources/styles.css finkmar@homedir.tngtech.com:public_html
scp -P 122 -r resources/src finkmar@homedir.tngtech.com:public_html
