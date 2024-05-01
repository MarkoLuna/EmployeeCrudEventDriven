# Employee Crud
Employee Crud using oauth2, web, lombok, H2 DB and others.

## Instructions
- Run EmployeeCrudApplication class
- Import "EmployeeCrud.postman_collection.json" file into postman tool
- Run any request on Postman tool (make sure that any environment is selected and run Login request before this)
- Swagger url http://localhost:8080/swagger-ui/index.html

## Prerequisites
- Open JDK 17

You can install any version, i.e. [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html).

- Maven

You can use the embedded Maven binaries that your IDE offers, or you can install your own one.

## Update versions
```
mvn versions:display-property-updates 
```

### Checkstyle 
```
mvn checkstyle:checkstyle
```

