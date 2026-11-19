# AGENTS.md

> Context for AI coding assistants working on **Spring Cloud Alibaba**.
> This file is maintained collaboratively by the community.
> See [issue #4313](https://github.com/alibaba/spring-cloud-alibaba/issues/4313).

---

## 1. Project Structure & Module Layout

This repository is a multi-module Maven project. Start by locating the module that owns the behavior you need to change before editing code.

### 1.1 Top-Level Repository Layout

```text
spring-cloud-alibaba/
|-- spring-cloud-alibaba-starters/       # Main implementation modules and Spring Boot starters
|-- spring-cloud-alibaba-tests/          # Integration-style test modules and shared test support
|-- spring-cloud-alibaba-examples/       # Runnable sample applications for major features
|-- spring-cloud-alibaba-dependencies/   # BOM and dependency version management
|-- spring-cloud-alibaba-coverage/       # JaCoCo aggregation module
|-- eclipse/                             # Eclipse formatter and related project settings
|-- .circleci/                           # CircleCI pipeline configuration
|-- .github/                             # GitHub workflows and issue/PR templates
|-- .mvn/                                # Maven Wrapper configuration
|-- pom.xml                              # Root POM
|-- mvnw / mvnw.cmd                      # Maven Wrapper entrypoints
|-- README.md / README-zh.md             # User-facing documentation
`-- Roadmap.md / Roadmap-zh.md           # Project roadmap
```

Important:

- `spring-cloud-alibaba-examples` is a sibling of `spring-cloud-alibaba-starters`, not a child of it.
- `spring-cloud-alibaba-coverage` is an aggregation module; do not add product logic there.

### 1.2 Inside `spring-cloud-alibaba-starters/`

Most feature work happens under `spring-cloud-alibaba-starters/`.

The modules in this directory do not all follow a single pattern. Use the actual module layout in the current branch instead of assuming every integration has the same split.

Common patterns:

- `spring-cloud-starter-*`
  - User-facing starter modules intended to be added as dependencies by applications.
- non-`starter` modules
  - Internal support modules, shared code, or feature-specific implementation modules used by starters.

Examples in `2025.1.x`:

- `spring-alibaba-nacos-config`
  - Core Nacos config support.
- `spring-cloud-starter-alibaba-nacos-config`
  - User-facing Nacos config starter built on top of the core module.
- `spring-cloud-starter-alibaba-nacos-discovery`
  - Nacos discovery and registration support.
- `spring-cloud-alibaba-commons`
  - Shared support code reused by multiple starters.
- `spring-cloud-circuitbreaker-sentinel`
  - Sentinel integration for Spring Cloud CircuitBreaker.
- `spring-cloud-alibaba-sentinel-datasource`
  - Sentinel datasource integration support.
- `spring-cloud-alibaba-sentinel-gateway`
  - Sentinel support for Spring Cloud Gateway.
- `spring-cloud-starter-bus-rocketmq`
  - Spring Cloud Bus integration over RocketMQ.
- `spring-cloud-starter-stream-rocketmq`
  - Spring Cloud Stream RocketMQ integration.

Contributor guidance:

- Some features use a core module plus a starter module.
- Some features implement functional code directly in the starter module.
- If you are tracing behavior from a public starter dependency, start in the matching `spring-cloud-starter-*` module, then follow its internal dependencies.

### 1.3 Example Module Families

The repository groups starter code and examples by feature area. Common families include:

- Nacos
- Sentinel
- RocketMQ
- Seata
- Sidecar
- SchedulerX

### 1.4 Where to Start Reading

- For dependency and version questions, start in `spring-cloud-alibaba-dependencies`.
- For runtime behavior, start in `spring-cloud-alibaba-starters`.
- For usage and reproduction flows, check `spring-cloud-alibaba-examples`.
- For integration-style verification coverage, check `spring-cloud-alibaba-tests`.

### 1.5 Impact Guidance

- Changes in `spring-cloud-alibaba-commons` can affect multiple starters.
- Changes in a `spring-cloud-starter-*` module usually affect that integration's public auto-configuration surface.
- Changes in examples should generally not be used as the primary implementation location for product behavior.
