# Backend Wayfinding (Still Under Active Development)
Back-end of the app.

This is a Java Gradle project and is developed using intelliJ IDEA editor (https://www.jetbrains.com/help/idea/getting-started-with-gradle.html).

# Getting Started

> **Note**: Make sure you have completed the [intelliJ - Environment Setup](https://www.jetbrains.com/help/idea/getting-started.html) before proceeding.

## Step 1: Clone repository in intellij 

First, you will need to clone and setup this gradle project to be able to build this gradle project.

To start cloning and Gradle Build, you will need to follow the following steps:

1. Select checkout from version control on the welcome screen intelliJ.
2. Choose Git and provide the URL for the Git Repository (https://github.com/ecocompass/backend_wayfinding/).
3. Click clone and this will download the repo to local machine as well to be able to make local changes and be able to push the tested changes.
4. Open the project now and let the editor download the dependencies and build the image without any errors. Fix any errors and test using Postman.

### Postman Testing (LocalHost testing):

Request:

curl --location 'http://0.0.0.0:8080/hello' \
--header 'Content-Type: application/json' \
--data '{"latitude": 37.7749, "longitude": -122.4194}' 

Response:

Hello World! Response from MapEngine! Received coordinates: Latitude 37.7749, Longitude -122.4194, OpenWeatherMap Response: City: San Francisco, Country: US, Temperature: 11.18°C, Weather Description: overcast clouds Nearest DART Stations: Malahide: 53.4509, -6.15649 Portmarnock: 53.4169, -6.1512 Dublin Connolly: 53.3531, -6.24591 

You can also do a local build of Java Gradle Project before doing a docker build to check for any issues while building the project.

```bash

./gradlew build
```

## Step 2:  Build image (Dockerised Project) and push on storage to be pulled for deployment:

We need now to dockerize the project and push it to the registry like (DockerHub):

```bash
docker build -t your-dockerhub-username/your-project:latest 

docker push your-dockerhub-username/your-project:latest
```

if you face login/authorization issues try using `docker login`.
