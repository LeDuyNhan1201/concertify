```shell
gradle wrapper --gradle-version 8.14
./gradlew clean build
./gradlew clean build -x test
./gradlew clean build -Dquarkus.native.enabled=true
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
```

```shell
source generate_ca.sh
sudo ./start.sh
sudo ./stop.sh
```

```shell
kafka-topics --bootstrap-server broker1:39091 --list --command-config /tmp/configs/superuser.properties
kafka-topics --bootstrap-server broker1:39091 --describe --topic <topic-name> --command-config /tmp/configs/superuser.properties
```
