#FROM eclipse-temurin:17-jdk-alpine as build
#WORKDIR /workspace/app
#
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle .
#COPY settings.gradle .
#COPY src src
#
#RUN chmod +x gradlew
#RUN ./gradlew build -x test
#RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)
#
#FROM eclipse-temurin:17-jre-alpine
#VOLUME /tmp
#ARG DEPENDENCY=/workspace/app/build/dependency
#COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
#COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
#ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.Application"]

# 1. OpenJDK 기반의 JDK 이미지 사용
FROM --platform=linux/amd64 openjdk:17-jdk-slim

# 2. JAR 파일을 컨테이너 내부로 복사
ARG JAR_FILE=build/libs/easy-table-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 3. 실행할 포트 지정
EXPOSE 8080

# 4. 실행 명령어 설정
ENTRYPOINT ["java", "-jar", "/app.jar"]
