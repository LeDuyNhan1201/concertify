ARG LOGSTASH_DOCKER_TAG

FROM docker.elastic.co/logstash/logstash:${LOGSTASH_DOCKER_TAG}
RUN logstash-plugin install logstash-output-opentelemetry