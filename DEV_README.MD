
## Local Development

Run the app:
```bash
./mvnw spring-boot:run
```

Run tests with coverage:
```bash
./mvnw clean test jacoco:report
```

### Linting

Check:
```bash
./mvnw spotless:check
```

Apply:
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

See example [`docker-compose.yaml`](docker/nas/docker-compose.yaml) and [`.env`](docker/nas/.env) in `docker/nas/`.

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
