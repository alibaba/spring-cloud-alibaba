# AGENTS.md

Guidance for AI coding agents (and humans) working on Spring Cloud Alibaba. It follows the structure proposed in [#4313](https://github.com/alibaba/spring-cloud-alibaba/issues/4313). Keep it short and factual, and update it in the same PR when a build step, module or convention changes.

## Build and test

The Maven wrapper is checked in, so use `./mvnw` (`mvnw.cmd` on Windows) rather than a local `mvn`.

- **JDK:** compiling needs **JDK 21.0.8 or later**. The enforcer plugin checks `compiler.jdk.version` in the root `pom.xml` because Error Prone and NullAway need it. The bytecode target is `java.version` (17), so the published artifacts still run on Java 17.
- **Maven:** 3.9.1 or later, which the wrapper provides.

```bash
./mvnw -B compile                          # compile everything; also runs Checkstyle (validate phase) and NullAway
./mvnw -B -pl <module> -am test            # build one module and its dependencies, then run its unit tests
./mvnw -B -pl <module> -am test -Dtest=ClassNameTests            # a single test class
./mvnw -B -pl <module> -am test -Dtest=ClassNameTests#methodName # a single test method
./mvnw -B install -DskipTests              # what CI does before running the test suite
./mvnw -B verify                           # full build with unit tests, as in .github/workflows/integration-test.yml
```

`<module>` is the module's path from the repo root, for example `spring-cloud-alibaba-starters/spring-cloud-alibaba-commons`.

## Project structure

```
spring-cloud-alibaba/
├── spring-cloud-alibaba-dependencies/   # BOM that users import; declares every component version
├── spring-cloud-alibaba-starters/       # One module per integration
│   ├── spring-cloud-alibaba-commons/    # Shared utilities used by the starters
│   ├── spring-alibaba-nacos-config/     # Core Nacos config logic (no Spring Cloud starter wiring)
│   ├── spring-cloud-starter-alibaba-nacos-config/     # User-facing starter built on the module above
│   ├── spring-cloud-starter-alibaba-nacos-discovery/
│   ├── spring-cloud-starter-alibaba-sentinel/, spring-cloud-circuitbreaker-sentinel/,
│   │   spring-cloud-alibaba-sentinel-datasource/, spring-cloud-alibaba-sentinel-gateway/
│   ├── spring-cloud-starter-stream-rocketmq/, spring-cloud-starter-bus-rocketmq/
│   ├── spring-cloud-starter-alibaba-seata/, spring-cloud-starter-alibaba-sidecar/,
│   │   spring-cloud-starter-alibaba-schedulerx/
├── spring-cloud-alibaba-examples/       # Runnable sample applications, one folder per component
├── spring-cloud-alibaba-tests/          # Integration tests that start real servers in Docker
│   └── spring-cloud-alibaba-test-support/   # JUnit 5 extensions shared by those tests
├── spring-cloud-alibaba-coverage/       # JaCoCo report aggregation
└── eclipse/                             # Formatter settings and Checkstyle suppressions
```

The parent is `org.springframework.cloud:spring-cloud-build`, so Checkstyle rules and much of the plugin setup come from Spring Cloud.

## Module dependencies and impact

- A change to `spring-cloud-alibaba-commons` can affect **every** starter, so run the full `./mvnw -B install -DskipTests` and then the tests of the starters you touched.
- Core logic lives in the non-starter module (for example `spring-alibaba-nacos-config`). When you change it, also build and test the starter that wraps it (`spring-cloud-starter-alibaba-nacos-config`).
- Adding or upgrading a third-party dependency means editing `spring-cloud-alibaba-dependencies/pom.xml`, not a child module.

## Testing

- Unit tests live in each module's `src/test/java` and must not need external services.
- Integration tests in `spring-cloud-alibaba-tests/` start Nacos, RocketMQ and other servers with Testcontainers. They are **skipped by default**. `HasDockerAndItEnabledCondition` only runs them when the `it.enabled=true` system property is set **and** Docker is reachable:

```bash
./mvnw -B -pl spring-cloud-alibaba-tests/nacos-tests -am test -Dit.enabled=true
```

- To reproduce a bug by hand, start from the matching app in `spring-cloud-alibaba-examples/` (each has a readme). For a local Nacos server, see the [Nacos quick start](https://nacos.io/en/docs/latest/quickstart/quick-start/).

## Conventions and common mistakes

- **Branches:** PRs target the branch for the Spring Boot line being fixed. `2025.1.x` is the default branch, and the README maps the older branches (`2025.0.x`, `2023.x`, `2022.x`, …) to their Spring Cloud and Spring Boot lines. Treat `pom.xml` (`revision`, `spring-boot.version`, `spring.cloud.version`) as the source of truth for versions, not prose docs.
- **Versions:** never hard-code a version in a child `pom.xml`. The project uses `${revision}` with `flatten-maven-plugin` (CI-friendly versions). Dependency versions belong in the BOM.
- **Checkstyle** runs in the `validate` phase, so any `./mvnw compile` fails fast on violations. Fix the code. Only add an entry to `eclipse/checkstyle-suppressions.xml` with a reviewer's agreement.
- **Null-safety:** NullAway runs as an Error Prone plugin in JSpecify mode, only for `@NullMarked` code (`-XepOpt:NullAway:OnlyNullMarked`). In `@NullMarked` packages, mark nullable parameters and returns with `@Nullable` instead of silencing the check.
- **Formatting:** follow the Eclipse formatter in `eclipse/eclipse-code-formatter.xml` (tabs, Spring style), as in surrounding files.
- **License header:** every new Java file starts with the Apache 2.0 header used across the codebase (`Copyright 2013-present the original author or authors.`).
- **PR description:** fill in `.github/PULL_REQUEST_TEMPLATE.md`: what and why, `Fixes #NNNN` if it fixes an issue, how you did it, and how to verify.
