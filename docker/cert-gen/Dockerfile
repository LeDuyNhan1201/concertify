FROM alpine:latest

# Install required packages
RUN apk add --no-cache \
    bash \
    openssl \
    openjdk17 \
    ca-certificates

# Set keytool in PATH (from openjdk)
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Dummy workdir; actual script is bind-mounted
WORKDIR /app

# No need to copy anything; everything is from volume
ENTRYPOINT [ "sh", "/app/generate_cert.sh" ]
