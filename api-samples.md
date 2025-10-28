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
            "687f48e8de7d23e5e3f74a9a",
            "687f48e8de7d23e5e3f74a8f",
            "687f48e8de7d23e5e3f74a90",
            "687f48e8de7d23e5e3f74a91"
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

```json
{
  "ids": [
    "687db518a938f9d7196acedc",
    "687db518a938f9d7196aced1",
    "687db518a938f9d7196aced2",
    "687db518a938f9d7196aced3"
  ]
}

;

{
  "oldItems": [
    "687f3d823cfdb45e5d5f87b7",
    "687f3d823cfdb45e5d5f87b8"
  ],
  "newItems": [
    {
      "seatId": "687db518a938f9d7196acf17",
      "seatCode": "STANDARD-R5-S2",
      "price": 60
    },
    {
      "seatId": "687db518a938f9d7196acf1a",
      "seatCode": "STANDARD-R5-S5",
      "price": 60
    }
  ]
}

```