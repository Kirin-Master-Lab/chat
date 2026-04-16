# Repository Guidelines

## Project Structure & Module Organization
`src/main/java/com/chenliang/chat` contains the main application code. Keep HTTP endpoints in `controller`, orchestration in `aiservice`, business logic in `service`, persistence interfaces in `mapper`, data models in `entity`, Spring setup in `config`, and LLM-callable helpers in `tools`. Static assets live in `src/main/resources/static`, and runtime configuration lives in `src/main/resources/application.properties`. Put tests under `src/test/java` using the same package layout as production code.

## Build, Test, and Development Commands
Run all commands from the repository root:

- `mvn spring-boot:run` starts the app locally on port `8082`.
- `mvn clean package -DskipTests` builds the JAR into `target/`.
- `mvn test` is the standard verification command.

At the moment, `mvn test` fails on the current branch because `AssistantController` has a compile-time `String` to `Flux<String>` mismatch. Fix that issue before treating the test target as a release gate.

## Coding Style & Naming Conventions
Follow the existing Java style: 4-space indentation, `UpperCamelCase` for classes, `lowerCamelCase` for methods and fields, and packages under `com.chenliang.chat.*`. Keep controllers thin, move business rules into services, and isolate external AI or dictionary integrations behind `aiservice` or `tools`. No formatter or linter plugin is configured in `pom.xml`, so keep imports clean and match surrounding code.

## Testing Guidelines
Add tests in `src/test/java` and name them `*Test.java` or `*Tests.java`. Prefer focused service and controller tests over only manual checks. There is no enforced coverage threshold yet, but each new feature should include at least one success-path test and, when practical, one failure-path assertion. Run `mvn test` before opening a pull request.

## Commit & Pull Request Guidelines
Recent history uses short `feat:` commit prefixes, for example `feat: V1`. Keep that style, but make subjects descriptive, such as `feat: add Redis-backed session state`. Pull requests should include a short summary, affected modules, local verification steps, linked issues, and screenshots or sample requests when API or UI behavior changes.

## Security & Configuration Tips
Do not commit real API keys, tokens, or environment-specific database credentials. Move secrets out of `application.properties` into environment variables or local overrides before sharing changes.
