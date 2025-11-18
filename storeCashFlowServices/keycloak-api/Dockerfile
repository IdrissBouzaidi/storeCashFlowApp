# Étape 1 : Build avec Maven et JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copier le fichier de configuration Maven et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le reste du projet
COPY src ./src

# Construire l'application sans exécuter les tests
RUN mvn clean package -DskipTests

# Étape 2 : Image légère avec JRE 21 pour l'exécution
FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker

# Copier le JAR généré depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port de l'application (à adapter si nécessaire)
EXPOSE 9090

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]