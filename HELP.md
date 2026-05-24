# Read Me First
The following was discovered as part of building this project:

* The JVM level was changed from '25' to '24' as the Kotlin version does not support Java 25 yet.

# Getting Started

curl -X GET http://localhost:8080/v1/categories/

curl -X POST http://localhost:8080/v1/categories \
-H "Content-Type: application/json" \
-d '{
"code": "FOOD",
"name": "Еда",
"type": "EXPENSE",
"isActive": true
}'

## Telegram bot commands

```
/categories
/b
/corr 800
/ex 20000
/category expense food Food
/category income salary Salary
/expense food 12.50 groceries
/income salary 1000 May salary
```

Expenses and incomes are saved to PostgreSQL first. If Google Sheets reporting is enabled, saved expenses are appended to the configured sheet.

## Environment variables

Local defaults use the PostgreSQL container from `docker-compose.yml`. On Railway, set these variables from your Postgres service and bot credentials:

```
JDBC_DATABASE_URL=jdbc:postgresql://host:port/database
JDBC_DATABASE_USERNAME=...
JDBC_DATABASE_PASSWORD=...
TELEGRAM_BOT_TOKEN=...
ALLOWED_TELEGRAM_USER_IDS=1234,2345
GOOGLE_SHEETS_ENABLED=true
GOOGLE_SHEETS_SPREADSHEET_ID=...
GOOGLE_SHEETS_RANGE=Transactions!A:E
GOOGLE_SHEETS_SERVICE_ACCOUNT_JSON={"type":"service_account",...}
```

Share the spreadsheet with the service account `client_email` before enabling Sheets reporting.



### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.5/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.5/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.5/reference/web/servlet.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.0.5/reference/actuator/index.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.5/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Flyway Migration](https://docs.spring.io/spring-boot/4.0.5/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
