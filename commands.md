```shell
# Setup global JAVA_HOME for the first time
nvim ~/.bashrc
export JAVA_HOME=/path/to/jdk-17.0.1
export PATH=$JAVA_HOME/bin:$PATH
source ~/.bashrc

# Make IDE has full permission on current project
sudo chown -R $USER:$USER path/to/project
sudo chmod +x *.sh

# Bonus
sudo ./start.sh
sudo ./stop.sh

sudo tar -czvf <project-root>.tar.gz <project-root>
sudo tar -xzf <project-root>.tar.gz

sudo apt install make curl jq

ssh root@<ip.address> "docker save <image-name> | gzip" | pv | gunzip | docker load
```

```shell
# Gradle scripts
# ./gradlew wrapper --gradle-version <gradleVersion>
# Check version on https://docs.gradle.org/current/userguide/gradle_wrapper.html
gradle wrapper --gradle-version 8.14.3

# ./gradlew :<module-name>:clean build -Dquarkus.native.enabled=true -x test
./gradlew clean build --warning-mode=all --stacktrace
./gradlew clean build -x test
./gradlew clean build -Dquarkus.native.enabled=true

# ./gradlew :<module-name>:quarkusDev
./gradlew :auth:quarkusDev
./gradlew :concert:quarkusDev
./gradlew :booking:quarkusDev

# Maven scripts
# mvn -N wrapper:wrapper -Dmaven=<maven-version>
# Check version on https://docs.gradle.org/current/userguide/gradle_wrapper.html
mvn -N wrapper:wrapper
./mvnw clean install

# ./mvnw -pl <module-name> quarkus:dev
./mvnw -pl auth quarkus:dev

# Without Jacoco
./mvnw -Dtest=<TestClassName>#<methodName1>+<methodName2> test

# With Jacoco
./mvnw clean verify -Dtest=<TestClassName>#<methodName1>+<methodName2> -Dquarkus.test.coverage.enabled=true

./mvnw clean verify -Dtest=AuthenticationControllerTest#verifyToken_withOrgContext_success -Dquarkus.test.coverage.enabled=true
```

```shell
# docker compose scripts
docker compose -f /path/to/real/docker-compose.yml up -d
docker compose -f /path/to/real/docker-compose.yml down -v

# docker scripts
docker logs -f <container-name>
docker stop <container-name>
docker rmi -f <container-name>
docker build -f /path/to/Dockerfile.builder -t quarkus/<service-name>-builder .

# docker exec -it -uroot <service-name> bash -c '<any-shell-scripts>';
docker exec -it -uroot auth-service bash -c 'cd /app && ./mvnw -pl auth quarkus:dev -Dquarkus.http.host=0.0.0.0 -DdebugHost=0.0.0.0 -DskipTests';

# Export realm.json require install 'jq' package first
docker exec -it keycloak /bin/bash 
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/export --realm concertify --users realm_file

# Inspect health check status require install 'jq' package first
docker inspect <container-name> | jq '.[0].State.Health.Log[] | {ExitCode, Output}'
```

```shell
docker exec -it broker1 /bin/bash 

# curl --insecure https://localhost:8081/subjects --cert /path/to/cert.pem --key /path/to/key.pem --cacert /path/to/ca.crt
curl --insecure https://localhost:8081/subjects --cert Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Desktop/Projects/concertify/docker/certs/ca/ca.crt

# curl -X DELETE --insecure https://localhost:8081/subjects/<schema-name> --cert /path/to/cert.pem --key /path/to/key.pem --cacert /path/to/ca.crt
curl -X DELETE --insecure https://localhost:8081/subjects/rollback.booking.updated.dlq-value --cert Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Desktop/Projects/concertify/docker/certs/ca/ca.crt

curl --insecure https://localhost:8081/subjects --cert Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Projects/concertify/docker/certs/ca/ca.crt

curl -X DELETE --insecure https://localhost:8081/subjects/booking.updated-value --cert Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Projects/concertify/docker/certs/ca/ca.crt

kafka-topics --bootstrap-server broker1:39091 --list --command-config /tmp/configs/superuser.properties
kafka-topics --bootstrap-server broker1:39091 --delete --topic 'booking.created' --command-config /tmp/configs/superuser.properties
kafka-topics --bootstrap-server broker1:39091 --describe --topic 'greeting' --command-config /tmp/configs/superuser.properties
kafka-console-producer --bootstrap-server broker1:39091 --topic 'greeting' --producer-property group.id=greeting-group --producer.config /tmp/configs/superuser.properties
kafka-console-consumer --bootstrap-server broker1:39091 --topic 'booking.created' --group 'concert_service.booking.created' --consumer.config /tmp/configs/superuser.properties
```