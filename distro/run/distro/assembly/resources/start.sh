#!/bin/bash

BASEDIR=$(dirname "$0")

# setup the JVM
if [ "x$JAVA" = "x" ]; then
  if [ "x$JAVA_HOME" != "x" ]; then
    echo Setting JAVA property to "$JAVA_HOME/bin/java"
    JAVA="$JAVA_HOME/bin/java"
  else
    echo JAVA_HOME is not set. Unexpected results may occur.
    echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
    JAVA="java"
  fi
fi


# set environment parameters
webappsPath=$BASEDIR/../lib/webapps/
restPath=$BASEDIR/../lib/rest/
classPath=$BASEDIR/../lib/db/,$BASEDIR/../keystore
optionalComponentChosen=false


# inspect arguments
while [ "$1" != "" ]; do
  case $1 in 
    --webapps ) optionalComponentChosen=true
                classPath=$webappsPath,$classPath
                echo WebApps enabled
                ;;
    --rest )    optionalComponentChosen=true
                classPath=$restPath,$classPath
                echo REST API enabled
                ;;
    * )         exit 1
  esac
  shift
done

# if neither REST nor Webapps are explicitly chosen, enable both
if [ "$optionalComponentChosen" = "false" ]; then
  echo REST API enabled
  echo WebApps enabled
  classPath=$webappsPath,$restPath,$classPath
fi

echo classpath: $classPath

# start the application
"$JAVA" -Dloader.path="$classPath" -jar "$BASEDIR/../lib/camunda-rest-distro.jar" --spring.config.location=file:"$BASEDIR"/../config/application.yml