SHELL:=/bin/bash
VERSION:=$(shell cat ../../src/build.gradle | grep -o "version = '.*'" | sed "s/version = '\(.*\)'/\1/")

dev-auth:
	docker exec -it -uroot auth-service bash -c 'cd /app && ./gradlew quarkusDev -DdebugHost=0.0.0.0 -DskipTests';

build-images:
	docker build -f ../../src/auth/src/main/docker/Dockerfile.builder -t quarkus/auth-builder ../..
    docker build -f ../../src/concert/src/main/docker/Dockerfile.builder -t quarkus/concert-builder ../..
    docker build -f ../../src/booking/src/main/docker/Dockerfile.builder -t quarkus/booking-builder ../..

remove-images:
	docker rmi quarkus/auth-builder
    docker rmi quarkus/concert-builder
    docker rmi quarkus/booking-builder

gen-cert:
    docker exec -uroot -it auth-service bash -c 'echo -e "password" | keytool -import -alias ca -keystore "/opt/graalvm/lib/security/certs/ca" -file src/main/resources/certs/ca/ca.crt';
    docker cp ../../certs/ca/ca.crt auth-service:/app/src/main/resources/certs/ca.crt && docker exec -uroot -it auth-service bash -c 'echo -e "password" | keytool -import -alias ca -keystore "/opt/graalvm/lib/security/certs/ca" -file src/main/resources/certs/ca/ca.crt';