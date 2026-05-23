# Expbot

Telegram bot for tracking family expenses and income. The service stores transactions in PostgreSQL and can append saved expenses to a Google Spreadsheet.

## Features

- Add expense and income categories.
- Save expenses and incomes from Telegram commands.
- Persist data in PostgreSQL with Flyway migrations.
- Optionally report saved expenses to Google Sheets.
- Deployable to Railway.

## Requirements

- JDK 21
- Docker, required for local PostgreSQL and integration tests
- Telegram bot token from BotFather
- Google Cloud service account, only if Google Sheets reporting is enabled

## Bot Commands

```text
/categories
/b
/corr 800
/category expense food Food
/category income salary Salary
/expense food 12.50 groceries
/income salary 1000 May salary
```

## Configuration

The app reads configuration from environment variables. Local defaults are provided for PostgreSQL.

| Variable | Required | Description |
| --- | --- | --- |
| `JDBC_DATABASE_URL` | No locally, yes on Railway | PostgreSQL JDBC URL. Local default is `jdbc:postgresql://localhost:5432/expbot`. |
| `JDBC_DATABASE_USERNAME` | No locally, yes on Railway | PostgreSQL username. Local default is `expbot`. |
| `JDBC_DATABASE_PASSWORD` | No locally, yes on Railway | PostgreSQL password. Local default is `expbot`. |
| `TELEGRAM_BOT_TOKEN` | Yes for Telegram replies | Bot token from BotFather. |
| `GOOGLE_SHEETS_ENABLED` | No | Set to `true` to append transactions to Google Sheets. Default is `false`. |
| `GOOGLE_SHEETS_SPREADSHEET_ID` | If Sheets enabled | Spreadsheet id from the Google Sheets URL. |
| `GOOGLE_SHEETS_RANGE` | No | Append range. Default is `Transactions!A:E`. |
| `GOOGLE_SHEETS_SERVICE_ACCOUNT_JSON` | If Sheets enabled | Full service account JSON as a single environment variable. |

## Build

Run tests and build the project:

```bash
./gradlew test
./gradlew bootJar
```

The executable jar is created under:

```text
build/libs/
```

Integration tests use Testcontainers and require Docker to be running.

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the service:

```bash
./gradlew bootRun
```

The service starts on port `8080`.

Check categories:

```bash
curl http://localhost:8080/v1/categories/
```

Stop local PostgreSQL:

```bash
docker compose down
```

Use `docker compose down -v` only when you also want to delete the local database volume.

## Telegram Webhook

The webhook endpoint is:

```text
POST /telegram/webhook
```

After deployment, register the webhook with Telegram:

```bash
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook?url=https://YOUR_PUBLIC_DOMAIN/telegram/webhook"
```

Check webhook status:

```bash
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getWebhookInfo"
```

## Google Sheets Setup

1. Create a Google Cloud service account.
2. Create a service account key in JSON format.
3. Copy the full JSON into `GOOGLE_SHEETS_SERVICE_ACCOUNT_JSON`.
4. Create a spreadsheet with a sheet named `Transactions`, or change `GOOGLE_SHEETS_RANGE`.
5. Share the spreadsheet with the service account `client_email`.
6. Set `GOOGLE_SHEETS_ENABLED=true`.

Rows are appended with these columns:

```text
date, time, category_description, amount, description
```

## Deploy to Railway

1. Create a new Railway project.
2. Add a PostgreSQL service.
3. Add this repository as an application service.
4. Configure the application environment variables:

```text
JDBC_DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>
JDBC_DATABASE_USERNAME=<username>
JDBC_DATABASE_PASSWORD=<password>
TELEGRAM_BOT_TOKEN=<telegram-bot-token>
GOOGLE_SHEETS_ENABLED=false
```

5. If Google Sheets reporting is needed, also set:

```text
GOOGLE_SHEETS_ENABLED=true
GOOGLE_SHEETS_SPREADSHEET_ID=<spreadsheet-id>
GOOGLE_SHEETS_RANGE=Transactions!A:E
GOOGLE_SHEETS_SERVICE_ACCOUNT_JSON=<full-service-account-json>
```

6. Deploy the service.
7. Register the Telegram webhook using the Railway public domain:

```bash
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook?url=https://YOUR_RAILWAY_DOMAIN/telegram/webhook"
```

Flyway runs automatically on application startup and creates or updates the database schema.

## Useful Commands

```bash
./gradlew test
./gradlew bootRun
./gradlew bootJar
docker compose up -d postgres
docker compose down
```
