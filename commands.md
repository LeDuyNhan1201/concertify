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
--data-urlencode 'username=organizer.us@gmail.com' \
--data-urlencode 'password=123' \
--data-urlencode 'client_id=web-app' \
--data-urlencode 'grant_type=password'

curl --insecure -X POST 'https://localhost:8443/realms/concertify/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=customer.us@gmail.com' \
--data-urlencode 'password=123' \
--data-urlencode 'client_id=web-app' \
--data-urlencode 'grant_type=password'

curl --insecure -X POST 'https://localhost:8443/realms/concertify/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=admin@gmail.com' \
--data-urlencode 'password=123' \
--data-urlencode 'client_id=web-app' \
--data-urlencode 'grant_type=password'

curl --insecure -X 'POST' \
  'https://localhost:61002/v1/users' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer ' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "organizer1.us@gmail.com",
  "password": "123",
  "firstName": "Organizer1",
  "lastName": "US",
  "group": "ORGANIZERS",
  "region": "US"
}'

curl --insecure -X 'POST' \
  'https://localhost:61002/v1/users' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer ' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "organizer2.us@gmail.com",
  "password": "123",
  "firstName": "Organizer2",
  "lastName": "US",
  "group": "ORGANIZERS",
  "region": "US"
}'

curl --insecure -X 'POST' \
  'https://localhost:61002/v1/users' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer 123' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "customer1.us@gmail.com",
  "password": "123",
  "firstName": "Customer1",
  "lastName": "US",
  "group": "CUSTOMERS",
  "region": "US"
}'

curl --insecure -X 'POST' \
  'https://localhost:61002/v1/users' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer 123' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "customer2.us@gmail.com",
  "password": "123",
  "firstName": "Customer2",
  "lastName": "US",
  "group": "CUSTOMERS",
  "region": "US"
}'

curl --insecure -X 'POST' \
  'https://localhost:62002/v1/organizer/concerts' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer ' \
  -H 'Content-Type: application/json' \
  -d '{
  "title": "Test",
  "description": "string",
  "location": "Ho Chi Minh",
  "startTime": "2022-03-10T12:15:50",
  "endTime": "2022-03-10T12:15:50"
}'

curl --insecure 'https://localhost:62002/v1/organizer/concerts/687d0fbc0ebd36ab870d270d' \
--header 'Authorization: Bearer '

curl --X PUT --insecure 'https://localhost:62002/v1/seats/hold/687db518a938f9d7196aceb9/concert' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ' \
--data '{
    "ids": [
            "687db518a938f9d7196acedc",
            "687db518a938f9d7196aced1",
            "687db518a938f9d7196acf18",
            "687db518a938f9d7196acf19"
        ]
}'

curl --insecure --X PUT 'https://localhost:62002/v1/seats/book/687d0fbc0ebd36ab870d270d/concert' \
--header 'Content-Type: application/json' \
--header 'Accept-Language: en-US' \
--header 'Authorization: Bearer ' \
--data '{
    "ids": [
            "687d0fbc0ebd36ab870d2730",
            "687d0fbc0ebd36ab870d2725",
            "687d0fbc0ebd36ab870d276f",
            "687d0fbc0ebd36ab870d2770"
        ]
}'

curl --insecure --X PUT 'https://localhost:63002/v1/bookings/687d1a579c031f0b1a834a69' \
--header 'Content-Type: application/json' \
--header 'Accept-Language: vi-VN' \
--header 'Authorization: Bearer ' \
--data '{
    "oldItems": [
    "687d1fbf357e30a0f2a52688",
    "687d1fbf357e30a0f2a52689"
  ],
  "newItems": [
    {
      "seatId": "687d0fbc0ebd36ab870d272f",
      "seatCode": "VIP-R1-S1",
      "price": 120
    },
    {
      "seatId": "687d0fbc0ebd36ab870d270e",
      "seatCode": "VIP-R1-S1",
      "price": 120
    }
  ]
}'

curl --insecure --X DELETE 'https://localhost:63002/bookings/v1/687d1a579c031f0b1a834a69' \
--header 'Authorization: Bearer '
```

```shell
source generate_ca.sh
sudo ./start.sh
sudo ./stop.sh
```

```shell
docker exec -it broker1 /bin/bash 
curl --insecure https://localhost:8081/subjects --cert Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Desktop/Projects/concertify/docker/certs/ca/ca.crt

curl -X DELETE --insecure https://localhost:8081/subjects/booking.created-value --cert Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Desktop/Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Desktop/Projects/concertify/docker/certs/ca/ca.crt

curl --insecure https://localhost:8081/subjects --cert Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Projects/concertify/docker/certs/ca/ca.crt

curl -X DELETE --insecure https://localhost:8081/subjects/booking.updated-value --cert Projects/concertify/docker/kafka/schema-registry1/certs/cert.pem --key Projects/concertify/docker/kafka/schema-registry1/certs/key.pem --cacert Projects/concertify/docker/certs/ca/ca.crt

kafka-topics --bootstrap-server broker1:39091 --list --command-config /tmp/configs/superuser.properties
kafka-topics --bootstrap-server broker1:39091 --delete --topic 'booking.created' --command-config /tmp/configs/superuser.properties
kafka-topics --bootstrap-server broker1:39091 --describe --topic 'greeting' --command-config /tmp/configs/superuser.properties
kafka-console-producer --bootstrap-server broker1:39091 --topic 'greeting' --producer-property group.id=greeting-group --producer.config /tmp/configs/superuser.properties
kafka-console-consumer --bootstrap-server broker1:39091 --topic 'booking.created' --group 'concert_service.booking.created' --consumer.config /tmp/configs/superuser.properties
```
