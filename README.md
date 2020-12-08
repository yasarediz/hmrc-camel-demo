All you need to do is to execute the following shell script within the project folder:

* ./run.sh


This shell script will:
* BUILD THE APP
* BUILD THE DOCKER IMAGE
* AND RUN THE APP IN DOCKER CONTAINER


If you would like to do all the above steps manually, please run the following commands:

* mvn clean install
* docker build -t hmrccameldemo:1.0 .
* docker run -p 8080:8080 hmrccameldemo:1.0
