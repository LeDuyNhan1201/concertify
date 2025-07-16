```shell
gradle wrapper --gradle-version 8.14
./gradlew clean build
./gradlew clean build -x test
./gradlew clean build -Dquarkus.native.enabled=true
./gradlew :common:clean build -x test
./gradlew :auth:quarkusDev
./gradlew :concert:quarkusDev
./gradlew :booking:quarkusDev

sudo tar -czvf concertify.tar.gz concertify
sudo tar -xzf concertify.tar.gz
```

```shell
docker compose up -d
docker compose down -v
docker logs -f postgres
docker stop postgres
docker rm -f -v postgres

docker exec -it keycloak /bin/bash 
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/export --realm concertify --users realm_file

docker inspect schema-registry1 | jq '.[0].State.Health.Log[] | {ExitCode, Output}'
```

```shell
curl --insecure -X POST 'https://localhost:8443/realms/concertify/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=organizer.vi@gmail.com' \
--data-urlencode 'password=123' \
--data-urlencode 'client_id=web-app' \
--data-urlencode 'grant_type=password'

curl --insecure -X POST 'https://localhost:8443/realms/concertify/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=customer.vi@gmail.com' \
--data-urlencode 'password=123' \
--data-urlencode 'client_id=web-app' \
--data-urlencode 'grant_type=password'

curl --insecure -X POST 'https://localhost:8443/realms/concertify/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=admin@gmail.com' \
--data-urlencode 'password=123' \
--data-urlencode 'client_id=web-app' \
--data-urlencode 'grant_type=password'
```

```shell
source generate_ca.sh
sudo ./start.sh
sudo ./stop.sh
```

```shell
docker exec -it broker1 /bin/bash 
curl --insecure https://localhost:8081/subjects --cert Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Desktop/Projects/concertify/docker/certs/ca/ca.crt
kafka-topics --bootstrap-server broker1:39091 --list --command-config /tmp/configs/superuser.properties
kafka-topics --bootstrap-server broker1:39091 --describe --topic 'greeting' --command-config /tmp/configs/superuser.properties
kafka-console-producer --bootstrap-server broker1:39091 --topic 'greeting' --producer-property group.id=greeting-group --producer.config /tmp/configs/superuser.properties
kafka-console-consumer --bootstrap-server broker1:39091 --topic 'greeting' --group 'greeting-group' --consumer.config /tmp/configs/superuser.properties
```
