# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vampire-squid is a REST-based video asset manager for tracking videos from mission-based camera deployments. It's part of MBARI's Video Annotation and Reference System (M3). The service manages video metadata, file references, and temporal information critical for scientific research.

**Data Model Hierarchy:**
```
VideoSequence (deployment/session)
  └─> Video (temporal segment with start time + duration)
      └─> VideoReference (concrete file reference with URI, codec, dimensions, etc.)
```

Key concept: A single deployment is typically broken into segments, with multiple representations (master, mezzanine, proxies) of each segment tracked via VideoReference entities.

## Build & Development Commands

**Build:**
```bash
./sbtx compile          # Build project (or `sbt compile` if sbt installed)
sbt stage              # Stage application for packaging
```

**Testing:**
```bash
./sbtx test                    # Run all unit tests
./sbtx itPostgres/test        # Run PostgreSQL integration tests
./sbtx itSqlserver/test       # Run SQL Server integration tests
```

**Run single test with tag:**
```scala
// Add tag to test:
val todoTag = new munit.Tag("todo")
test("delete".tag(todoTag)):
  assert(true)
```
```bash
testOnly org.mbari.vampiresquid.repository.jpa.DerbyVideoDAOSuite -- --include-tags=todo
```

**Run application locally:**
```bash
./sbtx run              # Runs Main.scala, starts HTTP server on port 8080
```

**Docker build:**
```bash
./build.sh              # Runs sbt stage, builds and pushes Docker image
```

## Architecture

**Technology Stack:**
- Scala 3.7.3 with Tapir (type-safe REST endpoints)
- JPA/Hibernate for database access (supports PostgreSQL, SQL Server, Oracle)
- Flyway for database migrations
- VertX for async HTTP server
- JWT-based authentication

**Code Organization:**

**Domain layer** (`vampire-squid/src/main/scala/org/mbari/vampiresquid/domain/`):
- `Media.scala` - Flattened view combining VideoSequence + Video + VideoReference data
- `VideoSequence.scala`, `Video.scala`, `VideoReference.scala` - Core domain objects
- `MoveVideoParams.scala` - Parameters for moving video references between videos

**Repository layer** (`vampire-squid/src/main/scala/org/mbari/vampiresquid/repository/`):
- `jpa/JPADAOFactory.scala` - Creates DAOs, manages EntityManagerFactory
- `jpa/entity/` (Java) - JPA entities: VideoSequenceEntity, VideoEntity, VideoReferenceEntity
- `jpa/*DAOImpl.scala` - DAO implementations with transaction management
- DAOs follow trait pattern with base interfaces in `repository/` package

**Controller layer** (`vampire-squid/src/main/scala/org/mbari/vampiresquid/controllers/`):
- Business logic layer between endpoints and repositories
- `MediaController.scala` - Primary controller, handles complex create/update operations across all three entity types
- `VideoSequenceController.scala`, `VideoController.scala`, `VideoReferenceController.scala` - Entity-specific operations
- Controllers manage transactions and coordinate between multiple DAOs

**Endpoints layer** (`vampire-squid/src/main/scala/org/mbari/vampiresquid/endpoints/`):
- Tapir endpoint definitions (type-safe HTTP routes)
- `MediaEndpoints.scala` - Main API surface, exposes Media (flattened) view
- `VideoSequenceEndpoints.scala`, `VideoEndpoints.scala`, `VideoReferenceEndpoints.scala` - Entity-specific endpoints
- `AuthorizationEndpoints.scala` - JWT token generation
- Each endpoint class defines both endpoint schemas and their implementations

**Application entry points:**
- `Main.scala` - HTTP server startup, VertX configuration
- `Endpoints.scala` - Wires controllers/endpoints, separates blocking vs non-blocking endpoints for VertX
- `AppConfig.scala` - Configuration loading from environment variables

**Key patterns:**
1. **Media as primary API**: Most operations use the `Media` case class (flattened view) rather than exposing the three-level hierarchy directly. `MediaController.create()` handles creating VideoSequence/Video/VideoReference as needed.

2. **DAO sharing**: DAOs can share EntityManagers via `newVideoDAO(dao: DAO[?])` pattern for cross-entity transactions.

3. **VertX endpoint separation**: Endpoints in `Endpoints.scala` are separated into `nonBlockingEndpoints` (auth, health) and `blockingEndpoints` (database operations) for VertX's threading model.

4. **Database migrations**: Flyway migrations in `vampire-squid/src/main/resources/db/migrations/{postgres,sqlserver}/`

## Configuration

Configuration via Typesafe Config with environment variable overrides (see `vampire-squid/src/main/resources/reference.conf`):

**Required environment variables:**
- `DATABASE_DRIVER` - JDBC driver class
- `DATABASE_URL` - JDBC connection string
- `DATABASE_USER`, `DATABASE_PASSWORD` - Database credentials
- `BASICJWT_CLIENT_SECRET` - JWT client secret for authentication
- `BASICJWT_SIGNING_SECRET` - JWT signing secret

**Optional:**
- `HTTP_PORT` - Server port (default: 8080)
- `DATABASE_LOG_LEVEL` - Hibernate logging level (default: INFO)

## API Documentation

Swagger UI available at: `http://localhost:8080/docs` when running

Prometheus metrics at: `http://localhost:8080/metrics`

## Database Support

Project uses JPA with Hibernate and supports multiple databases:
- PostgreSQL (primary)
- Microsoft SQL Server
- Oracle (legacy support in commented-out itOracle module)

Database schema managed by Flyway migrations in `vampire-squid/src/main/resources/db/migrations/`

Integration tests use Testcontainers to spin up database instances.

## Code Style

Project uses:
- `.scalafmt.conf` for Scala formatting
- `.editorconfig` for cross-editor consistency
- Scala 3 syntax (indentation-based, new control structure syntax)

Format code: `./sbtx scalafmtAll`
