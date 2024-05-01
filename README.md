
# Backend Wayfinding

- The backend component of the application focuses on handling traffic incidents and route calculations.
- This is a Java Gradle project and is developed using intelliJ IDEA editor (https://www.jetbrains.com/help/idea/getting-started-with-gradle.html).

### Project Overview

#### IncidentsController

This controller manages traffic incidents.

```
GET /api/transit/incidents: Retrieves traffic incidents.
POST /createIncident: Creates a new incident.
DELETE /deleteIncident/{incidentId}: Deletes an incident by ID.
GET /api/incidents: Retrieves all incidents.
```

#### RoutesController

This controller handles route calculations.

```
GET /api/routes: Calculates the shortest path between two coordinates.
GET /api/routes2: Provides transition route recommendations between two coordinates.
```

### Application Demo

Watch the application in action on YouTube: [Application Demo](https://www.youtube.com/watch?v=cGchnnwWjGk)

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/cGchnnwWjGk/0.jpg)](https://youtu.be/cGchnnwWjGk)

### Dependencies

- Java
- Spring Framework
- Gradle

### Getting Started

> **Note**: Make sure you have completed the [intelliJ - Environment Setup](https://www.jetbrains.com/help/idea/getting-started.html) before proceeding.

#### Clone repository in intellij 

First, you will need to clone and setup this gradle project to be able to build this gradle project.

To start cloning and Gradle Build, you will need to follow the following steps:

1. Select checkout from version control on the welcome screen intelliJ.
2. Choose Git and provide the URL for the Git Repository (https://github.com/ecocompass/backend_wayfinding/).
3. Click clone and this will download the repo to local machine as well to be able to make local changes and be able to push the tested changes.
4. Open the project now and let the editor download the dependencies and build the image without any errors. Fix any errors and test using Postman.


#### Build image (Dockerised Project) and push on storage to be pulled for deployment:

We need now to dockerize the project and push it to the registry like (DockerHub):

```bash
docker build -t your-dockerhub-username/your-project:latest 

docker push your-dockerhub-username/your-project:latest
```

if you face login/authorization issues try using `docker login`.

#### Contributing

Contributions are welcome! Please follow the contribution guidelines. And contact us incase!

#### License
This project is licensed under the MIT License.
