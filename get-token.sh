#!/bin/bash

# Kiểm tra nếu thiếu email
if [ -z "$1" ]; then
  echo "Usage: $0 <email>"
  exit 1
fi

EMAIL="$1"

# Gửi request lấy token và trích xuất access_token
access_token=$(curl --insecure -s -X POST 'https://localhost:8443/realms/concertify/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "username=${EMAIL}" \
  --data-urlencode 'password=123' \
  --data-urlencode 'client_id=web-app' \
  --data-urlencode 'grant_type=password' | jq -r '.access_token')

# Kiểm tra nếu access_token không rỗng thì ghi vào file
if [ -n "$access_token" ] && [ "$access_token" != "null" ]; then
  echo "$access_token" > token.txt
  echo "Access token saved to token.txt"
else
  echo "Failed to retrieve access token"
  exit 1
fi
