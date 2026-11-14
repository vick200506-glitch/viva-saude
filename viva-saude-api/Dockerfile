# ---------- Etapa 1: compilar ----------
# Imagem com Maven + Java 21 só para gerar o .jar (ela não vai para a imagem final)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro só o pom.xml e baixa as bibliotecas.
# Assim o Docker reaproveita esse passo (cache) enquanto o pom.xml não mudar.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Agora o código, e gera o .jar (os testes rodam fora do Docker, com mvn test)
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: rodar ----------
# Imagem final bem menor: só o Java para rodar (JRE), sem Maven nem código-fonte
FROM eclipse-temurin:21-jre
WORKDIR /app

# Segurança: a API roda com um usuário comum, não como root
RUN useradd --system --uid 1001 app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
