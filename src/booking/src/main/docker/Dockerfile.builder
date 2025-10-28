# Check image tag on https://quay.io/repository/quarkus/ubi-quarkus-native-image?tab=tags
ARG BUILDER_IMAGE=quay.io/quarkus/ubi-quarkus-native-image:21.3-java17
FROM $BUILDER_IMAGE

WORKDIR /app

# Install xargs (findutils)
USER root
RUN microdnf install -y findutils && microdnf clean all
USER quarkus

# Copy root Gradle configs
COPY gradlew settings.gradle dependencies.gradle build.gradle gradle.properties /app/
COPY gradle/ /app/gradle/

# Copy submodules build.gradle
COPY src/auth/build.gradle /app/src/auth/
COPY src/concert/build.gradle /app/src/concert/
COPY src/booking/build.gradle /app/src/booking/

# (optional) copy sources nếu muốn compile trong container
COPY src/auth/src /app/src/auth/src
COPY src/concert/src /app/src/concert/src
COPY src/booking/src /app/src/booking/src

# Pre-download dependencies and plugins for caching
RUN ./gradlew --no-daemon :booking:dependencies --refresh-dependencies || true
RUN ./gradlew --no-daemon :booking:quarkusBuild --dry-run || true

ENTRYPOINT [ "sh" ]

