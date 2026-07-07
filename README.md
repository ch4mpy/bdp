# BdP-labs : TPs de création d'APIs REST avec Spring Boot

#### Pré-requis

- [Git](https://git-scm.com/install/). Sur Windows, Git bash avec Mingw
- [nvm](https://www.nvmnode.com/fr/guide/download.html)
- [SDKMan](https://sdkman.io/install/)
- Docker ou [Docker Desktop](https://docs.docker.com/desktop/)
- une entrée `127.0.0.1 host.docker.internal` dans `/etc/hosts` (`C:\windows\system32\drivers\etc\hosts` sous Windows)

```bash
sdk env install
bash ./deploy-dev.sh
```

## 1. Build avec Maven

### 1.1. Introduction

Par convention, bien que déclarés dans le module parent, les modules d'un projet Maven suivent l'arborescence de répertoires.
```
api/
|─ bff
|─ rest-api
```
Ce qui est défini dans le pom parent sert de valeur par défaut pour les modules.

#### Structure
Un _"artifact"_ (livrable) est identifié par ses `groupId`, `artifactId` et `version`.

Les valeurs de `packaging` généralement utilisées sont `pom` et `jar` (avant Boot, on utilisait aussi `war` ou `ear` en fonction du serveur de déploiement).

Les `licenses`, `developers` et `scm` sont essentiellement informatives (bien que le dernier puisse être utilisé par des plugins tels que `release`).

`properties` est un ensemble de clef-valeur libres qui peuvent être référencées n'importe où dans le module où elles sont définies, ou dans les modules enfant. Spring Boot définit de très nombreuses version de librairies de cette manière. Maven fournie quelques properties contextuelles telles que `project.basedir`, `project.groupId`, `project.artifactId` et , `project.version`.

Les `modules` enfants à inclure lors de l'exécution des phases d'un module parent doivent être déclarés.

Le `dependencyManagement` permet de définir des versions par défaut pour un module et ses enfants. On peut y importer un `dependencyManagement` d'un autre POM avec une dépendnance de `type` `pom` et un `scope` de type `import` (`spring-cloud-dependencies` par exemple).

`dependencies`, à la racine du `project`, déclare les dépendances effectives d'un module. Le `scope` d'une dépendance indique comment elle est fournie et quand elle est utilisée:
- `compile` : valeur par défaut, la dépendnace est toujours inclue
- `provided` : fournie à l'exécution, généralement par le conteneur (par exemple la `servlet-api` est déjà dans Tomcat) => présent à la compilation et dans les test mais pas dans le jar
- `runtime` : absent lors de la compilation mais présents lors des tests et dans le jar
- `test` : présent uniquement lors de la compilation des tests et de leur exécution
- `import` : pour référencer un _artifact_ de type `pom`

La section `build` permet de contrôler l'assemblage du projet, notamment via ses sections `plugins` (et `pluginManagement`) et `resources`.

La section `profiles` permet de surcharger toute partie du build pour certaines exécutions. Dans les TPs, nous utilisons le profile `openapi` pour ajouter des dépendances à SpringDoc-OpenAPI, lancer l'application avant les test d'intgration, récupérer la spec OpenAPI sur la swagger-ui; puis arrêter l'application après les tests d'intégration.

#### Phases
- `validate` : intégrité des POMs
- `compile` : peut comprendre des manipulations de ressources et de la génération de code
- `test` : exécution des tests unitaires et d'intégration
- `package` : assemblage du jar / war
- `verify` : assertions sur l'état de sortie de l'environnement de build (peu utilisé)
- `install` : copie des packages dans le repo local
- `deplo` : export des packages sur le repos distants

L'exécution d'une phase implique celle de toutes les phases précédentes. `mvn install` et `mvn validate compile test package verify install` reviennent donc au même.

Il est possible de "sauter" les tests avec l'option `-DskipTests`: `mvn install -DskipTests`.

L'éxécution d'une phase sur un module provoque son exécution sur l'ensemble de ses modules enfants. Pour exécuter un module spécifique, préciser son nom avec l'option `-pl` mais attention, pour que les dépendances soient aussi assemblées, il faut ajouter `-am`. Par exemple (`mvn install -pl rest-api -am`)

### 1.2. Dépendances

Spring Boot gère la compatibilité d'un très grand nombre de dépendances. Pour initier un projet, utiliser https://start.spring.io ou un plugin équivalent de l'IDE.

Dépendances utilisées durant les TPs:
- `spring-boot-starter-webmvc` : appli web
- `spring-boot-starter-validation` : validation des entrées
- `spring-boot-starter-data-jpa` : ORM et accès à la BDD
- `spring-boot-starter-oauth2-resource-server` : autorisation d'accès aux ressources REST
- `spring-boot-starter-cache`
- `spring-boot-starter-actuator` : liveness et readiness probes
- `spring-boot-starter-opentelemetry`, `opentelemetry-logback-appender-1.0` et `spring-boot-starter-aspectj` : observabilité et `@Observed`
- `spring-boot-starter-restclient` et `spring-boot-starter-oauth2-client` : consomation d'un service REST distant autorisé avec OAuth2 (utilisateurs dans Keycloak via son Admin API)
- `spring-cloud-dependencies`et `spring-cloud-starter-gateway-server-webmvc` : Gateway pour routage avec `TokenRelay`
- `swagger-annotations` : documentation OpenAPI
- `spring-boot-devtools` : redémarre l'app après édition de code
- `spring-boot-configuration-processor` : génère les méta-données des `application.properties` à partir des `@ApplicationProperties`
- `lombok` : réduction de la verbosité Java
- `spring-addons-starter-oidc` : auto-configuration OIDC supplémentaire
- `wiremock-spring-boot` : stub pour la configuration OIDC de l'OP
- `mapstruct` : mapping automatique
- `jspecify` : null saftey

### 1.3. Processeurs d’annotations à la compilation

`lombok`, `mapstruct`, `spring-boot-configuration-processor` et `therapi-runtime-javadoc-scribe` génèrent du code à partir d'annotations. Il faut indiquer au `maven-compiler-plugin` l'ordre dans lequel les appliquer (par exemple Mapstruct utilise les accesseurs générés par Lombok).

### 1.4. Génération de spec OpenAPI à partir du code source

Swagger peut générer des specs OpenAPI à partir de code Java. Il expose cette spec au runtime. Par défaut, la spec elle-même est disponible sur `/v3/api-docs`.

Spring ayant de nombreuses conventions qui lui sont propres et qui induise un comportement qui lui est propre, il faut ajouter des métadonnées. Une partie est générée automatiquement par `springdoc-openapi`, mais il faut souvent completer avec des annotations Swagger.

Pour éviter tou impact au runtime, la dépendances à `springdoc-openapi-starter-webmvc-api` et l'exécution du `springdoc-openapi-maven-plugin` sont isolées dans un `profile` Maven.

Le `springdoc-openapi-maven-plugin` s'exécute pendant la phase `verify` en récupérant la spec OpenAPI sur `/v3/api-docs`. Il faut donc préalablement démarrer l'application (utilisation du `spring-boot-maven-plugin`). Les clients OIDC ayant besoin de récupérer la configuration OpenID du provider, nous utiliserons le `wiremock-maven-plugin` pour en exposer une. 

```xml
<profile>
	<id>openapi</id>
	<properties>
		<integration-tests.port>8080</integration-tests.port>
	</properties>
	<dependencies>
    <dependency>
      <groupId>com.github.therapi</groupId>
      <artifactId>therapi-runtime-javadoc</artifactId>
    </dependency>
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-api</artifactId>
		</dependency>
		<dependency>
			<groupId>com.c4-soft.springaddons</groupId>
			<artifactId>spring-addons-starter-openapi</artifactId>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>uk.co.automatictester</groupId>
				<artifactId>wiremock-maven-plugin</artifactId>
				<executions>
					<execution>
						<id>start-wiremock</id>
						<phase>pre-integration-test</phase>
						<goals>
							<goal>run</goal>
						</goals>
						<configuration>
							<dir>src/test/resources/wiremock</dir>
							<params>--port=8089</params>
						</configuration>
					</execution>
					<execution>
						<id>stop-wiremock</id>
						<phase>post-integration-test</phase>
						<goals>
							<goal>stop</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.springdoc</groupId>
				<artifactId>springdoc-openapi-maven-plugin</artifactId>
				<configuration>
					<apiDocsUrl>
						${integration-tests.scheme}://${integration-tests.hostname}:${integration-tests.port}/v3/api-docs</apiDocsUrl>
				</configuration>
				<executions>
					<execution>
						<id>integration-test</id>
						<goals>
							<goal>generate</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<profiles>h2</profiles>
				</configuration>
				<executions>
					<execution>
						<id>pre-integration-test</id>
						<goals>
							<goal>start</goal>
						</goals>
						<configuration>
							<arguments>
								<argument>
									--issuer=http://localhost:8089/auth/realms/labs</argument>
							</arguments>
							<environmentVariables>
								<!--SERVER_SSL_ENABLED>false</SERVER_SSL_ENABLED-->
							</environmentVariables>
						</configuration>
					</execution>
					<execution>
						<id>post-integration-test</id>
						<goals>
							<goal>stop</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</profile>
```

### 1.5. Génération de code client à partir de spec OpenAPI

Le `openapi-generator-maven-plugin` permet de générer beaucoup de code à partir d'un spec OpenAPI. Ici nous nous intéressons aux interfaces `@HttpExchange` dont Spring sait générer des implémentations.

Voici son management dans le POM parent :
```xml
<plugin>
	<groupId>org.openapitools</groupId>
	<artifactId>openapi-generator-maven-plugin</artifactId>
	<version>${openapi-generator-maven-plugin.version}</version>
	<configuration>
		<generatorName>spring</generatorName>
		<cleanupOutput>false</cleanupOutput>
		<skipIfSpecIsUnchanged>true</skipIfSpecIsUnchanged>
		<generateApiTests>false</generateApiTests>
		<generateModelDocumentation>false</generateModelDocumentation>
		<generateModelTests>false</generateModelTests>
		<generateSupportingFiles>false</generateSupportingFiles>
		<configOptions>
			<documentationProvider>none</documentationProvider>
			<annotationLibrary>none</annotationLibrary>
			<ensureUniqueParams>true</ensureUniqueParams>
			<generateBuilders>false</generateBuilders>
			<interfaceOnly>true</interfaceOnly>
			<library>spring-http-interface</library>
			<openApiNullable>false</openApiNullable>
			<serializableModel>true</serializableModel>
			<skipDefaultInterface>true</skipDefaultInterface>
			<useJakartaEe>true</useJakartaEe>
			<useOptional>true</useOptional>
		</configOptions>
	</configuration>
</plugin>
```
Il faut ensuite l'exécuter dans chaque module avec :
```xml
<plugin>
	<groupId>org.openapitools</groupId>
	<artifactId>openapi-generator-maven-plugin</artifactId>
	<executions>
		<execution>
			<goals>
				<goal>generate</goal>
			</goals>
			<configuration>
				<skipValidateSpec>true</skipValidateSpec>
				<inputSpec>
					${project.basedir}/../keycloak-admin-api.openapi.json</inputSpec>
				<apiPackage>org.keycloak.admin.api</apiPackage>
				<modelPackage>org.keycloak.admin.model</modelPackage>
				<!-- prevent a name colllision between the
				deprecated authTime (Integer) and the new auth_time
				(Long) in IDToken -->
				<nameMappings>auth_time=authTimeLong</nameMappings>
			</configuration>
		</execution>
	</executions>
</plugin>
```
La spec exposée par Keycloak étant imparfaite et le code généré comprenant des imports inutilisés qui posent des problèmes de compilation, on applique le `fmt-maven-plugin`:
```xml
<plugin>
	<groupId>com.spotify.fmt</groupId>
	<artifactId>fmt-maven-plugin</artifactId>
	<version>2.29</version>
	<configuration>
		<sourceDirectory>target/generated-sources</sourceDirectory>
		<verbose>true</verbose>
		<filesNamePattern>.*\.java</filesNamePattern>
		<skip>false</skip>
		<skipSourceDirectory>false</skipSourceDirectory>
		<skipTestSourceDirectory>true</skipTestSourceDirectory>
		<skipSortingImports>false</skipSortingImports>
		<style>google</style>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>format</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

### 1.6. Manipulation des ressources
Par défaut, Maven utilise les ressources de `src/main/resources` et `src/test/resources` telles quelles. Il est possible de modifier ce comportement dans le `buils`. Par exemple :
```xml
<resources>
	<resource>
		<directory>src/main/resources</directory>
	</resource>
	<resource>
		<directory>../../certs</directory>
	</resource>
</resources>
```

## 2. Modèles objet-relationnel et accès aux données
### 2.1. diagramme entité-relation d’une base de données relationnelle
### 2.2. rappels sur les fomres normales
### 2.3. diagramme de classes UML
```
+-----------------+       +--------------+      +--------------+
|    UserRoles    |<>---->|     Role     |<>--->|  Permission  |
|-----------------|       |--------------|      |--------------|
|userSub: String  |       |label: String |      |label: String |
+-----------------+       +--------------+      +--------------+
```
### 2.4. rappels sur les patrons de conception (design patterns)
### 2.5. mapping des propriétés
### 2.6. relations entre entités
### 2.7. conversion de types
### 2.8. @Repositoy Spring Data JPA
### 2.9. JPA query methods
### 2.10. spécifications JPA
### 2.11. Mise en cache

## 3. Web avec Spring Boot
### 3.1. injection de dépendances
### 3.2. convertisseurs automatiques de Spring
### 3.3. validation des entrées
### 3.4. gestion des exceptions
### 3.5. génération de la documentation OpenAPI
### 3.6. appels de services REST externes
