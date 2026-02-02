# ESM Project - Spring Boot REST API

This is a Spring Boot REST API application for Employee Submission Management (ESM) system.

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.4.3
- **Database**: PostgreSQL
- **Database Migration**: Flyway
- **Security**: Spring Security with BCrypt password encoding
- **API Documentation**: Swagger/OpenAPI (springdoc-openapi)
- **Build Tool**: Gradle

## Features

- User registration with BCrypt password encoding
- RESTful API architecture
- Automated database migration with Flyway
- API documentation with Swagger UI
- Dockerized application for easy deployment

## Database Migration with Flyway

The project uses Flyway for database schema version control. Migration scripts are located in `src/main/resources/db/migration` and are automatically executed when the application starts.

Migration naming convention:
- `V1__init_schema.sql` - Initial schema with users and related tables
- `V2__add_new_feature.sql` - Further migrations should follow this pattern

To add a new migration:
1. Create a new SQL file in `src/main/resources/db/migration`
2. Name it following the pattern `V{number}__{description}.sql`
3. Add your SQL statements
4. The migration will be applied automatically on application startup

## Local Development with Docker Compose

The project is configured to run locally using Docker Compose, which sets up:
- PostgreSQL database server
- Spring Boot API application

### Prerequisites

- Docker and Docker Compose installed on your machine
- JDK 21 for local development without Docker

### Running the Application

1. Clone the repository:
```bash
git clone https://github.com/manhtq99/esm_project.git
cd esm_project
```

2. Start the application using Docker Compose:
```bash
docker-compose up -d
```

This will start the PostgreSQL database and the Spring Boot API service.

3. Check the application logs:
```bash
docker-compose logs -f
```

4. To stop the application:
```bash
docker-compose down
```

5. To remove volumes (will delete database data):
```bash
docker-compose down -v
```

### Environment Variables

The application uses the following environment variables, which can be configured in the `docker-compose.yml` file or `application.yml`:

- `DB_HOST`: PostgreSQL host (default: localhost)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: Database name (default: esm_db)
- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)
- `JWT_SECRET`: JWT secret key for authentication

### API Documentation

Once the application is running, the API documentation is available at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Health Check

The application includes Spring Boot Actuator for monitoring:

- **Health**: http://localhost:8080/actuator/health
- **Info**: http://localhost:8080/actuator/info

## Running Without Docker

If you prefer to run the application without Docker:

### 1. Set up PostgreSQL database

Install PostgreSQL and create a database:
```sql
CREATE DATABASE esm_db;
```

### 2. Update environment variables

Set the following environment variables or update `application.yml`:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=esm_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

### 3. Run the application using Gradle

```bash
./gradlew bootRun
```

Or on Windows:
```bash
gradlew.bat bootRun
```

## Building for Production

To build the application JAR file:

```bash
./gradlew clean build
```

The JAR file will be created in the `build/libs` directory.

To build without running tests:
```bash
./gradlew clean build -x test
```

To run the JAR file:
```bash
java -jar build/libs/esm_project-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### User Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/api/users/register` | Register new user account | Public |

### Example: Register New User

**Request:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123",
    "fullName": "John Doe",
    "role": "EMPLOYEE"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "role": "EMPLOYEE",
  "status": "ACTIVE"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-02-02T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists"
}
```

## Database Schema

The application uses the following main tables:

- **users** - User accounts with BCrypt encrypted passwords
- **form_templates** - Form template definitions
- **template_fields** - Dynamic fields for each template
- **workflow_configs** - Approval workflow configurations
- **submissions** - Employee form submissions
- **approval_logs** - Approval history logs

## Security

- User passwords are encrypted using BCrypt before storing in the database
- Spring Security is configured to allow public access to registration endpoint
- Other endpoints require authentication (to be implemented)
- CSRF protection is disabled for API endpoints (consider enabling for production)

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/esm_project/
│   │   ├── config/          # Security and application configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handlers
│   │   ├── repository/      # Data access layer
│   │   └── service/         # Business logic layer
│   └── resources/
│       ├── db/migration/    # Flyway migration scripts
│       └── application.yml  # Application configuration
└── test/                    # Unit and integration tests
```

### Code Standards

- Follow Spring Boot best practices
- Use Lombok to reduce boilerplate code
- Implement proper validation using Jakarta Validation
- Use constructor injection for dependencies
- Write clean, simple, and readable code

## Troubleshooting

### Database Connection Issues

If you encounter database connection errors:
1. Ensure PostgreSQL is running
2. Check database credentials in environment variables
3. Verify the database exists: `psql -U postgres -l`

### Flyway Migration Errors

If Flyway migrations fail:
1. Check the migration SQL syntax
2. Verify migration files are in the correct directory
3. Use `./gradlew flywayClean` to reset (WARNING: deletes all data)
4. Use `./gradlew flywayMigrate` to manually run migrations

### Port Already in Use

If port 8080 is already in use:
1. Change the port in `application.yml`: `server.port: 8081`
2. Or kill the process using port 8080

## License

This project is for educational purposes.

## Contact

For questions or issues, please contact the development team.
