#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*: org.classiclude.tools.accountmanager.SQLAccountManager
