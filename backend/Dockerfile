# Imagem do back-end para o Render (Java nao e runtime nativo do Render; o servico roda como Docker).

# 1) Build: compila e empacota o jar com o Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src src
# os testes rodam no desenvolvimento (mvn test); aqui so empacota
RUN mvn -q -B -DskipTests package

# 2) Execucao: so a JRE e o jar, rodando com usuario sem privilegios
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/moeda-estudantil-*.jar app.jar
USER app

# O plano gratuito do Render tem 512 MB e 0,1 CPU: heap limitado a 70% da memoria do container,
# coletor serial (menos threads) e compilacao JIT mais leve para subir mais rapido.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:ReservedCodeCacheSize=48m"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
