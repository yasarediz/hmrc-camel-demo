#!/usr/bin/env bash

PROJECT_DIR="hmrc-camel-demo"

#STAGE 1 - BUILD THE APP
echo "Building ${PROJECT_DIR} app..."
mvn clean install

rc=$?
if [ $rc -ne 0 ]; then
  echo 'Maven build failed. Exiting!'
  exit $rc
fi

#STAGE 2 - BUILD THE DOCKER IMAGE
echo "Building Docker image for ${PROJECT_DIR} app..."

docker build -t hmrccameldemo:1.0 .

#STAGE 3 - RUN THE APP IN DOCKER CONTAINER
echo "Running the container..."
docker run -p 8080:8080 hmrccameldemo:1.0

echo "ALL DONE!"
exit
