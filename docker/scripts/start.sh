#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
env_file=${DIR}/helper/env_config.sh
echo "Processing $env_file"
source "$env_file"
source "${DIR}"/helper/functions.sh
create_client_files
create_env_file

docker compose up -d