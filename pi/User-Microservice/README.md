# User Microservice CI/CD

This module contains the Spring Boot backend for the user service.

## Local build

- `./mvnw test`
- `./mvnw -DskipTests package`

## Docker

Build the backend image from `pi/User-Microservice`:

- `docker build -t user-microservice:latest .`

## Kubernetes

Apply the backend manifests from `pi/User-Microservice/k8s`:

- `kubectl apply -f k8s/mysql.yaml`
- `kubectl apply -f k8s/deployment.yaml`
- `kubectl apply -f k8s/service.yaml`

## Jenkins

Create a Pipeline job named `Jungle-UserService` and point the SCM script path to:

- `pi/User-Microservice/Jenkinsfile.backend`

Configure these Jenkins tools/credentials before running the pipeline:

- `JDK-21`
- `Maven-3.9`
- `SonarQube`
- `docker-registry-credentials`
- `kubeconfig-credentials`

