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

Par convention, bien que déclarés dans le module parent, les modules d'un projet Maven suivent l'arborescence de
répertoires.

```
api/
|─ bff
|─ service-common
|─ account-service
|─ card-payment-service
|─ customer-service
|─ user-service
```

Ce qui est défini dans le pom parent sert de valeur par défaut pour les modules.

#### Structure

Un _"artifact"_ (livrable) est identifié par ses `groupId`, `artifactId` et `version`.

Les valeurs de `packaging` généralement utilisées sont `pom` et `jar` (avant Boot, on utilisait aussi `war` ou `ear` en
fonction du serveur de déploiement).

Les `licenses`, `developers` et `scm` sont essentiellement informatives (bien que le dernier puisse être utilisé par des
plugins tels que `release`).

`properties` est un ensemble de clef-valeur libres qui peuvent être référencées n'importe où dans le module où elles
sont définies, ou dans les modules enfant. Spring Boot définit de très nombreuses version de librairies de cette
manière. Maven fournie quelques properties contextuelles telles que `project.basedir`, `project.groupId`,
`project.artifactId` et , `project.version`.

Les `modules` enfants à inclure lors de l'exécution des phases d'un module parent doivent être déclarés.

Le `dependencyManagement` permet de définir des versions par défaut pour un module et ses enfants. On peut y importer un
`dependencyManagement` d'un autre POM avec une dépendnance de `type` `pom` et un `scope` de type `import` (
`spring-cloud-dependencies` par exemple).

`dependencies`, à la racine du `project`, déclare les dépendances effectives d'un module. Le `scope` d'une dépendance
indique comment elle est fournie et quand elle est utilisée:

- `compile` : valeur par défaut, la dépendnace est toujours inclue
- `provided` : fournie à l'exécution, généralement par le conteneur (par exemple la `servlet-api` est déjà dans
  Tomcat) => présent à la compilation et dans les test mais pas dans le jar
- `runtime` : absent lors de la compilation mais présents lors des tests et dans le jar
- `test` : présent uniquement lors de la compilation des tests et de leur exécution
- `import` : pour référencer un _artifact_ de type `pom`

La section `build` permet de contrôler l'assemblage du projet, notamment via ses sections `plugins` (et
`pluginManagement`) et `resources`.

La section `profiles` permet de surcharger toute partie du build pour certaines exécutions. Dans les TPs, nous utilisons
le profile `openapi` pour ajouter des dépendances à SpringDoc-OpenAPI, lancer l'application avant les test d'
intégration,
récupérer la spec OpenAPI sur la swagger-ui; puis arrêter l'application après les tests d'intégration.

#### Phases

- **_validate_** : intégrité des POMs
- _initialize_ :
- _generate-sources_ :
- _process-sources_ :
- _generate-resources_ :
- _process-resources_ :
- **_compile_** :
- _process-classes_ :
- _generate-test-sources_ :
- _process-test-sources_ :
- _generate-test-resources_ :
- _process-test-resources_ :
- _test-compile_ :
- _process-test-classes_ :
- **_test_** : exécution des tests unitaires
- _prepare-package_ :
- **_package_** : assemblage du jar / war
- _pre-integration-test_ :
- _integration-test_ :
- _post-integration-test_ :
- **_verify_** : assertions sur l'état de sortie de l'environnement de build (peu utilisé)
- **_install_** : copie des packages dans le repo local
- **_deploy_** : export des packages sur le repos distants

L'exécution d'une phase implique celle de toutes les phases précédentes. `mvn install` et
`mvn validate compile test package verify install` reviennent donc au même.

Il est possible de "sauter" les tests avec l'option `-DskipTests`: `mvn install -DskipTests`.

L'éxécution d'une phase sur un module provoque son exécution sur l'ensemble de ses modules enfants. Pour exécuter un
module spécifique, préciser son nom avec l'option `-pl` mais attention, pour que les dépendances soient aussi
assemblées, il faut ajouter `-am`. Par exemple (`mvn install -pl rest-api -am`)

### 1.2. Dépendances

Spring Boot gère la compatibilité d'un très grand nombre de dépendances. Pour initier un projet,
utiliser https://start.spring.io ou un plugin équivalent de l'IDE.

Dépendances utilisées durant les TPs:

- `spring-boot-starter-webmvc` : appli web
- `spring-boot-starter-validation` : validation des entrées
- `spring-boot-starter-data-jpa` : ORM et accès à la BDD
- `spring-boot-starter-oauth2-resource-server` : autorisation d'accès aux ressources REST
- `spring-boot-starter-cache`
- `spring-boot-starter-actuator` : liveness et readiness probes
- `spring-boot-starter-opentelemetry`, `opentelemetry-logback-appender-1.0` et `spring-boot-starter-aspectj` :
  observabilité et `@Observed`
- `spring-boot-starter-restclient` et `spring-boot-starter-oauth2-client` : consomation d'un service REST distant
  autorisé avec OAuth2 (utilisateurs dans Keycloak via son Admin API)
- `spring-cloud-dependencies`et `spring-cloud-starter-gateway-server-webmvc` : Gateway pour routage avec `TokenRelay`
- `swagger-annotations` : documentation OpenAPI
- `spring-boot-devtools` : redémarre l'app après édition de code
- `spring-boot-configuration-processor` : génère les méta-données des `application.properties` à partir des
  `@ApplicationProperties`
- `lombok` : réduction de la verbosité Java
- `spring-addons-starter-oidc` : auto-configuration OIDC supplémentaire
- `wiremock-spring-boot` : stub pour la configuration OIDC de l'OP
- `mapstruct` : mapping automatique
- `jspecify` : null saftey

### 1.3. Processeurs d’annotations à la compilation

`lombok`, `mapstruct`, `spring-boot-configuration-processor` et `therapi-runtime-javadoc-scribe` génèrent du code à
partir d'annotations. Il faut indiquer au `maven-compiler-plugin` l'ordre dans lequel les appliquer (par exemple
Mapstruct utilise les accesseurs générés par Lombok).

### 1.4. Génération de spec OpenAPI à partir du code source

Swagger peut générer des specs OpenAPI à partir de code Java. Il expose cette spec au runtime. Par défaut, la spec
elle-même est disponible sur `/v3/api-docs`.

Spring ayant de nombreuses conventions qui lui sont propres et qui induise un comportement qui lui est propre, il faut
ajouter des métadonnées. Une partie est générée automatiquement par `springdoc-openapi`, mais il faut souvent completer
avec des annotations Swagger.

Pour éviter tou impact au runtime, la dépendances à `springdoc-openapi-starter-webmvc-api` et l'exécution du
`springdoc-openapi-maven-plugin` sont isolées dans un `profile` Maven.

Le `springdoc-openapi-maven-plugin` s'exécute pendant la phase `verify` en récupérant la spec OpenAPI sur
`/v3/api-docs`. Il faut donc préalablement démarrer l'application (utilisation du `spring-boot-maven-plugin`). Les
clients OIDC ayant besoin de récupérer la configuration OpenID du provider, nous utiliserons le `wiremock-maven-plugin`
pour en exposer une.

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
                        ${integration-tests.scheme}://${integration-tests.hostname}:${integration-tests.port}/v3/api-docs
                    </apiDocsUrl>
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
                                    --issuer=http://localhost:8089/auth/realms/labs
                                </argument>
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

Le `openapi-generator-maven-plugin` permet de générer beaucoup de code à partir d'un spec OpenAPI. Ici nous nous
intéressons aux interfaces `@HttpExchange` dont Spring sait générer des implémentations.

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
                    ${project.basedir}/../keycloak-admin-api.openapi.json
                </inputSpec>
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

La spec exposée par Keycloak étant imparfaite et le code généré comprenant des imports inutilisés qui posent des
problèmes de compilation, on applique le `fmt-maven-plugin`:

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

Par défaut, Maven utilise les ressources de `src/main/resources` et `src/test/resources` telles quelles. Il est possible
de modifier ce comportement dans le `buils`. Par exemple :

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

Les bases de données relationnelles sont modélisées avec des entités (tables) et des relations (clefs étrangères).

Le code Java comprend des relations qui ne sont pas modélisables directement avec une représentation entité-relation (
héritage, relations bi-directionnelles, ...)

La JPA (Java Persistence API) permet de faire le pont entre les deux représentations (classes VS entité-relation). Il
permet l'ORM (Object-Relational Mapping).

### 2.1. `@Entity`

**La Javadoc JPA est excellente et contient de très nombreux exemples.** Il faut la consulter sans retenue.

Une entité est une classe mappée sur une table en base de donnée.

Son `@Id` correspond à la clef primaire de la table.

Elle doit avoir un constructeur par défaut (sans paramètre) dont la visibilité peut être restreinte.

Il est possible (et souvent recommandé) de limiter les méthodes `equals` et `hasCode` à la (ou aux) propriété(s) `@Id`.

```java

@Entity
@Table(name = "cards")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Card {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String number;

    @Column(nullable = false)
    @ToString.Include
    private String accountNumber;
  
  ...
}
```

### 2.2. Identifiants générés

H2 et Postgres utilisent les séquences pour les identifiants numériques auto-générés (pas de PK auto-incrémentée come
MySQL par exemple).

`@GenratedValue` indique qu'une valeur est fournie par la BDD lors du 1er enregistrement d'une entité. Elle est associée
à `@Id` et doit référencer un générateur (dans le cas H2 ou Postgres, une séquence).

Les séquences sont décrites avec `@Generator`.

```java

@Id
@GeneratedValue(generator = "cardPaymentSeq")
@SequenceGenerator(name = "cardPaymentSeq", sequenceName = "payment_seq", allocationSize = 1)
private Long id;
```

### 2.3. Relations

Une propriété ayant pour type une autre entité doit être décorées avec `OneToOne` ou `@ManyToOne`.

Une propriété ayant pour type une collection d'entités doit être décorées avec `OneToMany` ou `@ManyToMany`.

En cas de relation bidirectionnelle, il faut indiquer un `mappedBy` du côté _"faible"_ (`OneToMany` ou un des deux
`OneToOne`).

`@Embeddable` indique qu'une classe n'est pas mappée sur une table.
A la place, ses propriétés sont ajoutées aux colonnes de la table des entités dans lesquelles elle est `@Embedded`.

### 2.4. Conversion de types

Lorsqu'un objet est mappé sur un type simple en base, il possible définir un `@Converter(autoApply = true)` qui
implémente `AttributeConverter<E, C>`.

```java

@Converter(autoApply = true)
public class InstantStringAttributeConverter implements AttributeConverter<Instant, String> {

    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String dbData) throws DateTimeParseException {
        if (dbData == null) {
            return null;
        }
        return Instant.parse(dbData);
    }
}
```

### 2.5. `@Repository` Spring Data JPA

Leur rôle est de manipuler les données en base. Ce sont généralement des singletons générés par Spring à partir d'une
interface.

Il y a une arborescence d'interfaces qui apportent diverses fonctionnalités nous nous intéressons à
`JpaRepository<E, ID>` (CRUD, éventuellement paginé) et `JpaSpecificationExecutor<E>` (filtres avec spécifications).

### 2.6. JPA query methods

Spring Data expose
un [DSL pour les opérations simples sur les entités](https://docs.spring.io/spring-data/jpa/reference/repositories/query-keywords-reference.html).

```java
public interface CardPaymentJpaRepository extends JpaRepository<CardPayment, String> {

    Page<CardPayment> findByCardNumber(String cardNumber, Pageable pageable);

    List<CardPayment> findByCardNumberAndTimestampBetween(String cardNumber, Instant from, Instant to);
}
```

Cet exemple fonctionne dans les deux cas suivants:

- `CardPayment` a une propriété `cardNumber` de type `String`
- `CardPayment` a une propriété `card` d'un type complexe (par exemple `Card`) qui a lui même une propriété `number` de
  type `String`

### 2.7. Spécifications JPA

Lorsque la logique de filtrage devient trop complexe (notamment lors de l'application de critères optionnels), les _"
query methods"_ sont généralement inadaptées.

Les Spécifications JPA sont souvent plus adaptées.

Le `@Repository` qui les utilise doit implémenter `JpaSpecificationExecutor<E>`.

Je recommande d'exposer des factories sur le repo pour convertir les critères de filtre en spécification. Se reporter à
`MoneyTransferJpaRepository` pour un exemple.

### 2.8. Transactions

Les opérations d'accès aux données en base se font à l'intérieur d'une transaction.

On utilise `@Transactionnal` pour déclarer qu'une méthode doit être exécutée à l'intérieur d'une transaction.

Les relations (`OneToMany`, `OneToOne`, etc.) étant _lazy_ par défaut, il faut parcourrir le graph d'objet à l'intérieur
de la transaction dans laquelle la racine a été récupérée.

Le plus simple est généralement de décorer les méthodes de `@Controller` avec `@Transactionnal`.

### 2.9. Mise en cache

L'enjeu principal de la mise en cache est de gérer l'obsolescence des données. Il faut donc remplacer les données en
cache (ou supprimer des entrées) lors des accès en écriture, ce qui implique :

- avoir la maîtrise totale de ces écritures
- utiliser un cache distribué (Redis ?) en environnement distribué

Pour activer la mise en cache dans l'application (utiliser un autre `CacheManager` pour un cache distribué):

```java

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
```

Pour déclarer un ou plusieurs caches, on décore généralement une classe:

```java
@CacheConfig(cacheNames = {"bidulesParTruc", "bidulesParMachin"})
```

Pour indiquer que la valeur de retour peut ête mise en cache:

```java
@Cacheable(cacheNames = "bidulesParTruc")
```

Utiliser `@Caching` pour indiquer qu'une opération (`save`, `delete`, ...) nécessite des opérations sur le cache (
`evict` et / ou `put`)

Lorsqu'une classe expose une interface publique plus importante que nécessaire celà peut grandement compliquer la
gestion des caches. Je recommande dans ce cas de faire un proxy n'exposant que le strict nécessaire et gérer les caches
à
ce niveau.

## 3. Web avec Spring Boot

### 3.1. injection de dépendances

### 3.2. convertisseurs automatiques de Spring

### 3.3. validation des entrées

### 3.4. gestion des exceptions

### 3.5. génération de la documentation OpenAPI

### 3.6. appels de services REST externes
