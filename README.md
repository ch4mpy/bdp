# REST-hero : TPs de création d'APIs REST avec Spring Boot

Le support de TP est une banque en ligne simplifiée permettant de :
- gérer bénéficiaires d'un client
- effectuer des virements. Il n'y a pas de connexion à des services bancaires externes. Ces services nes sont même pas simulés. Les opérations de crédit / débit de ces comptes sont simplement ignorées.
- gérer des cartes de paiement pour chaque compte: créer, activer, désactiver
- effectuer des paiement par carte.

Elle est composée de :
- une interface graphique React
- une API REST composée de :
  * une `gateway`. Les requêtes (du frontend) préfixées avec `/gateway/bff` sont autorisées avec des cookies de session (`http-only=true`) et protégées contre le CSRF (cookie `XSRF-TOKEN` avec `http-only=false` et header `X-XSRF-TOKEN` requis pour pour les requêtes `POST`, `PUT` `PATCH` et `DELETE`). Les requêtes de clients OAuth2 (appels inter-services, Bruno, Postman, ...) préfixées avec `/gateway/m2m` sont autorisées avec un `Bearer` token dans le header `Authorization`. 
  * `sgcb-starter-service-common` est un starter Spring Boot qui contenant des classes et de l'auto-configuration partagée.
  * `customer-service` responsable des clients et de leurs bénéficiaires. Ce service ne stocke que les bénéficiaires dans sa base de données. Les clients sont des utilisateurs de Keycloak (lecture / écriture via l'API Keycloak).
  * `account-service` responsable des comptes bancaires et des transferts entre comptes.
  * `card-service`responsable des cartes et des paiements par carte.

Principales caractéristiques techniques de l'API REST:
- **Observable** : emmet des logs collectées dans Loki, les métriques des métriques dans Mimir et les traces dans Tempo, le tout visualisé dans Grafana.
- **Documentée** avec OpenAPI : permet aux clients de générer le code pour les consommer et aux développeurs de la visualiser dans une Swagger-UI.
- **Communicante** : Appels REST inter-service:
  * le `customer-service` utilise Keycloak pour accéder aux utilisateurs
  * l'`account-service` vérifie auprès du `customer-service` qu'un client existe avant de créer un compte à son nom
  * le `card-service` vérifie auprès de l'`account-service` qu'un compte existe avant de lui attacher une carte et lui déclare un transfert d'argent lors d'un paiement par carte.
- **Sécurisée** : chaque endpoint d'API vérifie l'identité attachée à la requête et ses relations éventuelles avec les ressources qu'elle cherche à manipuler pour prendre autoriser l'accès.
- **Persistante** : les objets métier sont sauvegardés dans PostgreSQL avec JPA. Les requêtes les plus complexes (filtres sur les paiements par carte et les mouvements entre comptes) sont construites avec des spécifications JPA.
- **Performante** : utilisation de caches (lorsque c'est pertinent).

#### Pré-requis

- [Git](https://git-scm.com/install/). Sur Windows, Git bash avec Mingw
- [nvm](https://www.nvmnode.com/fr/guide/download.html)
- [SDKMan](https://sdkman.io/install/)
- Docker ou [Docker Desktop](https://docs.docker.com/desktop/)
- une entrée `127.0.0.1 host.docker.internal` dans `/etc/hosts` (`C:\windows\system32\drivers\etc\hosts` sous Windows)
- un IDE (Eclipse STS, Visual Studio Code, IntelliJ Ultimate)

```bash
# Installation du bon JDK par SDKMan
sdk env install
# Installation de la bonne version de Node avec NVM
nvm install --lts
nvm use
# Déploiement dans Docker de l'infra (bases de données, Keycloak, reverse-proxy, Loki, Grafana, Tempo, Mimir)
bash ./deploy-dev.sh
# Génération des specs OpenAPI des services
cd api && mvn install -Popenapi,h2 && cd ..
# Récupération des sources du front
git submodule init && git submodule update
# Installation des dépendances du front et génération du code client de l'API
cd frontend && npm i && npm run api && cd ..
```

Les services Docker :
- https://host.docker.internal/ui/ le frontend React (`advisor`/`secret`)
- https://host.docker.internal/auth/admin/master/console/#/labs Keycloak (`admin`/`secret`)
- https://host.docker.internal/grafana
- https://host.docker.internal/mailpit

Dans [Keycloak](https://host.docker.internal/auth/admin/master/console/#/labs/realm-settings/email), éditer le mot de passe SMTP avec la valeur de `secrets/mail/password.txt`.

Pour démarrer le front depuis le répertoire `frontend`:
```bash
npm run dev
```

Pour démarrer les services de l'API depuis un IDE, surcharger le propriété `spring.datasource.password` avec la valeur du fichier `/secrets/rest-api/postgres_password.txt` dans une run config.

## 1. Build avec Maven

### 1.1. Introduction

Par convention, bien que déclarés dans le module parent, les modules d'un projet Maven suivent l'arborescence de
répertoires.

```
api/
|─ account-service
|─ card-service
|─ customer-service
|─ gateway
|─ sgcb-starter-service-common
```

Ce qui est défini dans le pom parent sert de valeur par défaut pour les modules (group-id, version, dépendances, etc.).

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

Spring ayant de nombreuses conventions qui lui sont propres, il faut ajouter des métadonnées. Une partie est générée automatiquement par `springdoc-openapi`, mais il faut souvent completer avec des annotations Swagger, notamment pour les request parameters convertis automatiquement par Spring Web.

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

Le Modèle object de Java n'est pas modélisables directement avec une représentation entité-relation (
héritage, relations bi-directionnelles, ...)

La JPA (Java Persistence API) permet de faire le pont entre les deux représentations (classes VS entité-relation). Il
permet l'ORM (Object-Relational Mapping).

**La Javadoc JPA est excellente et contient de très nombreux exemples.** Il faut la consulter sans retenue.

### 2.1. `@Entity`

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

Une propriété ayant pour type une autre entité doit être décorées avec `@OneToOne` ou `@ManyToOne`.

Une propriété ayant pour type une collection d'entités doit être décorées avec `@OneToMany` ou `@ManyToMany`.

En cas de relation bidirectionnelle, il faut indiquer un `mappedBy` du côté _"faible"_ (`@OneToMany` ou un des deux
`@OneToOne`).

`@Embeddable` indique qu'une classe n'est pas mappée sur une table. Ses propriétés sont ajoutées aux colonnes de la table des entités dans lesquelles elle est `@Embedded`.

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

Les relations (`@OneToMany`, `@OneToOne`, etc.) étant _lazy_ par défaut, il faut parcourir le graph d'objet à l'intérieur
de la transaction dans laquelle la racine a été récupérée.

Le plus simple est généralement de décorer les méthodes de `@Controller` avec `@Transactionnal`, mais la logique métier demande parfois plus de finesse (différentes méthode exécutées dans des transactions différentes pour que certaines soient `commit` alors que d'autre sont `rollback`).

### 2.9. Mise en cache

L'enjeu principal de la mise en cache est l'obsolescence des données. Il faut donc remplacer ou supprimer des données en
cache lors d'accès en écriture, ce qui implique :

- avoir la maîtrise totale de ces écritures
- utiliser un cache distribué (Redis ?) en environnement distribué

Je conseille de créer un cache par type de donnée et par index. 
Par exemple, si on souhaite accéder à des instances de `Bidule` soit par la valeur de leur propriété `truc`, soit par celle de lor propriété `machin`, on créera deux caches: `bidulesParTruc` et `bidulesParMachin`.

Pour activer la mise en cache dans l'application :

```java
@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  CacheManager cacheManager() {
    // utiliser un autre `CacheManager` pour un cache distribué
    return new ConcurrentMapCacheManager();
  }
}
```

Pour déclarer un ou plusieurs caches, on décore généralement une classe :

```java
@CacheConfig(cacheNames = {"bidulesParTruc", "bidulesParMachin"})
```

Pour indiquer que la valeur de retour peut ête mise en cache :

```java
@Cacheable(cacheNames = "bidulesParTruc")
```

Pour indiquer qu'une opération en écriture nécessite des opérations de mise à jour du cache :

```java
@Caching(put = @CachePut(cacheNames = "bidulesParTruc", key = "#bidule.truc"),
        evict = @CacheEvict(cacheNames = "bidulesParMachin", key = "#bidule.machin"))
```

Lorsqu'une classe expose une interface publique plus importante que nécessaire cela peut grandement compliquer la gestion des caches. Je recommande dans ce cas de faire un proxy n'exposant que le strict nécessaire et gérer les caches à ce niveau.

## 3. Services REST WebMvc avec Spring Boot

Nous nous intéressons ici aux `@RestController` qui forment la façade visible d'une API REST.

### 3.1. Injection de dépendances

Un `@RestController` est un singleton managé par Spring. On peut donc lui faire injecter tout autre `@Component` (`@Repository`, `@Service`, ...). Je recommande de faire l'injection par le biais du constructeur. Par exemple, pour l'`AccountController` qui a besoin de collaborer avec les `AccountRepository` pour manipuler les comptes en base de donnée, `AccountMapper` pour faire des conversions entre DTOs et objets métier et `CustomersApi` pour dialoguer avec le `customer-service` :

```java
@RestController
public class AccountController {
  private final AccountRepository accountRepo;
  private final AccountMapper accountMapper;
  private final CustomersApi customersApi;

  // Peut être remplacé par @RequiredArgsContructor de Lombok
  public AccountController(AccountRepository accountRepo, AccountMapper accountMapper, CustomersApi customersApi) {
    this.accountRepo = accountRepo;
    this.accountMapper = accountMapper;
    this.customersApi = customersApi;
  }
}
```

Spring s'occupe d'instancier les `@Bean` et `@Component` nécessaires dans le bon ordre.

Les composants fournis par le biais de l'injection de dépendances peuvent être facilement remplacés pendant les tests (`@MockitoBean`).

### 3.2. `@RequestMapping`

Avec les signatures et types de retour de méthode, `@RequestMapping` participe  à la définition d'un endpoint. On utilise préférentiellement `@RequestMapping` au niveau de la classe pour les définitions communes à toutes les méthodes et ses spécialisation `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` et `@PatchMapping` sur chaque méthode.

La signature et le type de retour de la méthode donne les informations sur les entrées / sortie et le `@RequestMapping` le reste, notamment :
- le verbe HTTP à employer
- le path du endpoint (attention, si le `@RequestMapping` au niveau de classe et un spécialisation au niveau de la méthode portent tous deux un path, c'est la concaténation qui est appliquée)
- les `MediaType` acceptés en entrée et ceux supportés en retour

Le bon emploi des verbes HTTP est important pour la sécurité Web : `GET` ne doit être employé que pour des opérations qui ne changent pas l'état du système.

Pour rappel :
- `GET` est attendu pour les opérations en lecture et ne doit pas porter de _body_. Par exemple, les critères de filtre devraient être portés par des _request parameters_. Il est attendu que la réponse à un `GET` soit en status `200 Ok` et ait un _body_.
- `POST` est attendu pour une création de ressource et devrait porter un _body_ avec les informations sur la ressource à créer. La réponse devrait être en status `201 Created`, ne pas avoir de _body_ et porter un header `Location` pointant vers la ressource créée.
- `PUT` est attendu pour une modification de ressource existante et devrait porter un _body_ avec les informations de mise à jour. Il attendu une réponse dans le champ `2xx` sans _body_ (par exemple `202 Accepted` ou `204 No-content`).
- `DELETE` est attendu pour supprimer une ressource et ne doit pas porter de _body_ (l'URL doit suffire à identifier la ressource à supprimer).. Il attendu une réponse dans le champ `2xx` sans _body_ (par exemple `202 Accepted` ou `204 No-content`).
- `PATCH` est attendu pour la mise à jour partielle d'un ressource (le propriétés manquantes du _body_ sont ignorées). Elle est peu usitée et parfois mal supportée par les clients, donc à éviter. 

Une différence notable entre `POST` et `PUT` est que lors de la répétition d'une requête `PUT` sur la même resource et avec le même _body_, seule la première requête devrait avoir un effet, alors que la répétition d'un `POST` doit créer autant de ressources (ou tenter de le faire).

### 3.3. Convertisseurs automatiques de Spring

Au niveau du protocole HTTP, toutes les valeurs sont des `String`. 

Spring Web comporte de nombreux mécanismes de (dé)sérialisation, notamment :
- JSON (avec Jackson) : convertie les `@RequestBody` et `@ResponseBody`
- le `FormatterRegistry` : enregistre des convertisseurs à appliquer sur les `@PathVariable` et `@RequestParam`.

Les `JpaRepository<E, ID>` pour les `@Entity` ayant `Integer`, `Long` ou `String` comme type sont enregistrés dans le `FormatterRegistry`. 

En considérant que `MyEntity` a un `@Id Long id` et qu'il existe un `JpaRepository<MyEntity, Long>`, alors un requête `GET /resources/42` est routée sur la fonction suivante où `entity` est l'entité avec l'identifiant `42` : 

```java
@GetMapping(/resources/{resourceId})
Resource getResource(@PathVariable("resourceId") MyEntity entity);
```

Il est possible d'ajouter des converters dans le registry en fournissant une `@Configuration` qui implémente `WebMvcConfigurer`. Par exemple, pour ajouter une conversion automatique d'un compte depuis son `Iban` (`AccountRepository extends JpaRepository<Account, Iban>`):

```java
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
  @Autowired(required = false)
  AccountRepository accountRepo;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringAccountConverter(accountRepo));
  }

  @RequiredArgsConstructor
  static class StringAccountConverter implements Converter<String, Account> {
    private final AccountRepository accountRepo;

    @Override
    public @Nullable Account convert(@Nullable String source) {
      return source == null ? null
          : accountRepo.findById(Iban.of(source))
              .orElseThrow(
                  () -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "Account %s is not known by the account-service".formatted(source)));
    }
  }
}
```

### 3.4. Validation des entrées

L'utilisation de `jakarta.validation` permet de détecter au plus tôt les requêtes avec des données invalides. Lorsqu'un paramètre ou attribut ne respecte pas les contraintes de validation, une `MethodArgumentNotValidException` ou `ConstraintViolationException` est levée.

```java
public record MoneyTransferRequest(
    @NotNull @IbanString String sourceIban,
    @NotNull @IbanString String destinationIban,
    @NotNull @Min(1) Long amount,
    @NotNull @CurrencyIso3 String currency,
    @NotEmpty @Size(min = 3, max = 256) String label) {
}
```
```java
@PostMapping(BASE_PATH)
public ResponseEntity<Void> transferMoneyBetweenAccounts(@RequestBody @Valid MoneyTransferRequest dto);
```

Il est possible de créer ses propres annotations de validation. De telles annotations doivent faire référence à un `ConstraintValidator` :

```java
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyIso3.IbanConstraintValidator.class)
public @interface CurrencyIso3 {
  String message() default "Doesn't look like an ISO 4217 currency code";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  public static class IbanConstraintValidator implements ConstraintValidator<CurrencyIso3, String> {
    @SuppressWarnings("null")
    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");

    @Override
    public boolean isValid(@Nullable String value, @Nullable ConstraintValidatorContext context) {
      if (value == null) {
        return true;
      }
      return ISO_4217.matcher(value).matches();
    }

  }
}
```

Je recommande de laisser passer `null` et de combiner les annotations avec `@NotNull` pour rendre un paramètre / attribut obligatoire.

### 3.5. Gestion des exceptions

Lorsqu'une erreur est détectée (technique ou métier), une exception est levée. 

Les exceptions sont interceptées dans deux cas principaux :
- on souhaite la contourner (il y a un _plan B_ pour continuer le traitement)
- on souhaite la mapper vers un autre type d'exception (exception technique traduite en exception métier)

Les `@ExceptionHandler` déclarés dans un `@RestControllerAdvice` servent intercepter des exceptions et à définir la réponse à envoyer :
```java
@RestControllerAdvice
public class CommonExceptionsHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    final var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    return ResponseEntity.status(detail.getStatus()).body(detail);
  }
}
```

Un raccourci lorsque créer et intercepter une exception métier n'a pas d'intérêt intrinsèque est de soulever une `ErrorResponseException` pour laquelle Spring fournit déjà un `@ExceptionHandler`.

### 3.6. Génération de la documentation OpenAPI

OpenAPI est un standard largement adopté issu de Swagger. Il permet notamment :
- aux développeurs de visualiser les caractéristiques d'une API REST
- aux clients de générer du code pour consommer l'API

`springdoc-openapi` permet de générer le plus gros de la spec à partir du code source en se basant sur les `@RequestMapping` et les signatures de méthodes. Il a quelques limitations auxquelles il faut pallier avec les annotation Swagger:
- il ne prend pas en compte le `FormatterRegistry`. Lorsqu'une `@PathVariable` ou un `@RequestParam` utilise une conversion implicite. Il faut utiliser `@Parameter` pour indiquer le format attendu dans la requête HTTP.
```java
@GetMapping(TRANSFER_PATH)
public MoneyTransferResponse getMoneyTransfer(
    @Parameter(schema = @Schema(type = "integer"), description = "The ID of the money transfer to retrieve")
    @PathVariable(name = TRANSFER_ID_PLACEHOLDER) MoneyTransfer transfer);
```

- il ne prend pas en compte nativement les `@RequestParam` implicites (plusieurs request-params encapsulés dans un pseudo DTO). Il faut utiliser `@ParameterObject`.
```java
@GetMapping(BASE_PATH)
public PagedModel<MoneyTransferResponse> listMoneyTransfers(
    // les propriétés de MoneyTransferFilterRequest et de Pageable sont attendues en tant que request-param individuels dans l'URL
    @Nullable @Valid @ParameterObject MoneyTransferFilterRequest dto,
    @ParameterObject Pageable pageable);
```

### 3.7. Appels de services REST externes

Le client REST actuellement recommandé pour les servlets est `RestClient`. `RestTemplate` est en mode maintenance et `WebClient` est plus adapté aux application réactives.

Il est possible de l'utiliser directement, mais Spring sait générer un client à partir d'une interface `@HttpExchange` et d'un `RestClient`. Spring Cloud proposait `@FeignClient` dans le même sprit, mais le projet est passé en mode maintenance depuis l'apparition des proxy d'`HttpExchange` générés.

Dans ce projet, nous générons les interfaces `@HttpExchange` à partir de specs OpenAPI (en utilisant l'`openapi-generator-maven-plugin`). Il est toutefois possible de déclarer ces interfaces manuellement, ce qui peut être utile lorsque l'API à consommer n'expose pas de spec OpenAPI.

Nous utilisons aussi [`spring-addons-starter-rest`](https://github.com/ch4mpy/spring-addons/tree/master/spring-addons-starter-rest) pour auto-configurer des instances de `RestClient` avec l'`application.yml`.

```java
@Configuration
public class RestConfiguration {

  @Bean
  // CustomersApi est une interface `@HttpExchange` générée à partir de la spec OpenAPI du customer-service
  // customerServiceClient est instancié par spring-addons à partir des propriétés customer-service-client ci-dessous
  CustomersApi customersApi(RestClient customerServiceClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(CustomersApi.class, customerServiceClient)
        .getObject();
  }
}
```
```yml
com:
  c4-soft:
    springaddons:
      rest:
        client:
          customer-service-client:
            base-url: ${reverse-proxy-uri}/gateway/m2m
            ssl-bundle: self-signed
            headers:
              Accept: 
                - application/json
                - application/problem+json
            authorization:
              oauth2:
                oauth2-registration-id: account-service
```

Lors d'une erreur pendant l'appel REST, `RestClient` soulève une `HttpClientErrorException`. Il est fortement recommandé d'intercepter de telles erreurs et de les translater vers quelque chose ajoutant des logs et du contexte métier.

```java
try {
    customersApi.getCustomer(dto.customerId());
} catch (HttpClientErrorException e) {
    if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
    log.warn("Rejecting account {} creation for unknown customer {}", iban, dto.customerId());
    throw new ResourceNotFoundException(
        "Customer %s is not known by the customer-service".formatted(dto.iban()));
    } else {
    log
        .error(
            "Unexpected error while checking customer {} existence in customer-service",
            dto.customerId(),
            e);
    }
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Unexpected error while checking customer existence",
        e);
}
```

### 3.8. Logs

Les logs sont la première source d'audit de l'application. Il est important de loguer suffisamment et au bon niveau: `error`, `warn`, `info`, `debug` ou `trace`.

`@Slf4j` de Lombok permet de disposer d'un logger nommé `log`.

Avec Spring Boot 4, `spring-boot-starter-opentelemetry` et `opentelemetry-logback-appender-1.0` permettent de pousser les logs vers Loki avec relativement peut de conf :
- `/src/main/resources/logback-spring.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>

    <appender name="OTEL" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="OTEL"/>
    </root>
</configuration>
```
- `/src/main/resources/application.yml`
```yml
management:
  opentelemetry:
    logging:
      export:
        otlp:
          endpoint: http://host.docker.internal:4318/v1/logs
```