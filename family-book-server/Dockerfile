# 朵攒攒后端 Dockerfile
# 既支持本地 Docker 部署，也支持微信云托管从源码直接构建镜像

FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml ./
COPY src ./src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata \
  && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
  && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=builder /build/target/family-book-server-*.jar app.jar

ENV SERVER_PORT=9000
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx768m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 9000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE} --server.port=${SERVER_PORT}"]
