#VERSION=0.1.0-SNAPSHOT
VERSION=latest
TAG=karolisl/babag-api:${VERSION}


lein-uberjar:
	lein uberjar


docker-build:
	docker build -t ${TAG} .


docker-push:
	docker push ${TAG}

docker: docker-build docker-push


all: lein-uberjar docker-build docker-push


.PHONY: lein-uberjar docker-build docker-push docker


