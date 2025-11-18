# Étape 1 : Build avec Maven et JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# ----------------------------------------
# 1. Copier uniquement les POMs (parent + modules)
#    Cela permet de cacher les dépendances Maven
# ----------------------------------------
COPY pom.xml .
COPY keycloak-api/pom.xml keycloak-api/
COPY min-io-api/pom.xml min-io-api/
COPY store-cash-flow-api/pom.xml store-cash-flow-api/
COPY user-api/pom.xml user-api/
# (ajoute ici les pom.xml de tous tes modules)

# Télécharger toutes les dépendances pour tous les modules
RUN mvn dependency:go-offline -B

# ----------------------------------------
# 2. Copier maintenant le code source
#    Cela n'invalide pas le cache Maven
# ----------------------------------------
COPY . .

# Construire uniquement le module ciblé (ex: minioapi)
# -pl = projet ciblé, -am = build également les modules dont il dépend
ARG MODULE_NAME
RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# Étape 2 : image légère avec JRE 21 pour exécution
FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker

# Copier le JAR généré du module ciblé
ARG MODULE_NAME
COPY --from=build /app/${MODULE_NAME}/target/*.jar app.jar

# c'est le port de debugging
# Il est préférable que je donne aussi le port de chaque projet à partir d'ici, mais ces ports sont définies sur le docker compose
# Càd avant le dockerfile.
EXPOSE 5005

# ➕ Ajouter l'option de debug JVM
# suspend=n : l'app démarre directement
# address=*:5005 : écoute sur tous les hôtes
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]