# Employee API (Shared Commons)

The `employee-api` module serves as the central contract repository for the **Employee CRUD Event-Driven System**. It provides a set of shared Data Transfer Objects (DTOs), enumerations, and exceptions to ensure consistency across microservices.

## Build & Development

### Standard Build
To build and install the library to your local Maven repository:
```bash
./mvnw clean install
```

### Custom Assertions (AssertJ)
This module uses the `assertj-assertions-generator-maven-plugin` to automatically generate fluent assertions for DTOs. These are generated during the `compile` phase and can be found in `target/generated-sources`.

### Code Formatting (Spotless)
We use Spotless with Google Java Format to maintain a consistent code style.
```bash
# Check formatting
./mvnw spotless:check

# Apply formatting
./mvnw spotless:apply
```

---

## Integration

To use this library in another module, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.employeecrud</groupId>
    <artifactId>employee-api</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## Prerequisites
- **Java 21**
- **Maven 3.8+**
