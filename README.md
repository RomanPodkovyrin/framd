
---

> **A note on the frontend:** I am a backend engineer. Frontend work is not my domain and not where I want to spend time on this project. The current UI was built with AI assistance and should be treated as a proof of concept and a template, not a finished design. It is functional enough to demonstrate the backend capabilities, but nothing more.
>
> If you are a frontend developer and feel like this project could use a proper UI, contributions are very welcome. The backend exposes a fully documented OpenAPI spec (see API Docs below), so you have everything you need to build against it. Whether you want to improve the existing HTMX/Thymeleaf templates or build a separate frontend entirely, that is welcome. If going separate, I would still prefer something lightweight and simple in spirit, like HTMX and Thymeleaf, over a heavy JS framework.

---
## Features

See [FEATURES.md](FEATURES.md) for the full list of planned and in-progress features.

## Local Development

```bash
./mvnw spring-boot:run
```
```bash
./mvnw clean test jacoco:report
```
### Linting

spotless
```bash
./mvnw spotless:check
```

```bash
./mvnw spotless:apply
```
## Spring Profiles

Two profiles are available: `dev` and `prod`.

- **dev**: local development
- **prod**: Docker/NAS deployment

### Activating a profile

**Mac/Linux:**
```bash
export SPRING_PROFILES_ACTIVE=dev
```

**IntelliJ IDEA:**

Go to `Run > Edit Configurations`, select your Spring Boot run configuration, and add `dev` to the `Active profiles` field.

## Docker
### Build
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t framd .
```

### Push to Docker Hub

```bash
docker login
docker tag framd romanempiredev/framd:0.1
docker tag framd romanempiredev/framd:latest
docker tag framd romanempiredev/framd:dev
docker push romanempiredev/framd:0.1
docker push romanempiredev/framd:latest
docker push romanempiredev/framd:dev

```
## API Docs

Once the app is running, the OpenAPI spec is available at:

- **Swagger UI**: http://localhost:7878/swagger-ui.html
- **JSON spec**: http://localhost:7878/v3/api-docs
- **YAML spec**: http://localhost:7878/v3/api-docs.yaml