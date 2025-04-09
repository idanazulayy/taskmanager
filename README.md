# Task Async Manager

This project is a Spring Boot 3 application built with Java 17, demonstrating asynchronous processing, multithreading, and Redis integration.

## Features

- Async execution of sub-tasks using `@Async`
- Redis persistence with TTL for sub-processes
- Real-time progress tracking of each task
- Dockerized for easy deployment

## API Endpoints

### `POST /tasks`
- Accepts a list of sub-processes
- Launches each in a separate thread
- Stores their info in Redis
- Returns a UUID of the task
- exmaple : 
```bash
 curl --location 'http://localhost:8080/tasks' \
--header 'Content-Type: application/json' \
--data '{
  "subProcess": [
    { "processName": "SendEmail" },
    { "processName": "GeneratePDF" },
    { "processName": "UploadToS3" }
  ]
}
'

```
### `GET /tasks/{taskId}`
- Returns remaining time and % completion of each sub-task
- Returns overall average progress
```bash
curl --location 'http://localhost:8080/tasks/9ac6d00f-57da-4cba-aea9-f3e561bfed10'
```

## 🐳 Run with Docker compose

Build and run:
```bash
docker-compose up --build

