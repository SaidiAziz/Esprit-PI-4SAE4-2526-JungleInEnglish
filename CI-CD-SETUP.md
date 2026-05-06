# Jungle Frontend - CI/CD Pipeline Setup

This document outlines the complete Jenkins CI/CD pipeline for the Jungle Frontend (PiFront) Angular application.

## Pipeline Stages Overview

The Jenkins pipeline consists of the following stages:

1. **Checkout** — Clone the DevOps_FrontEnd branch
2. **Install Dependencies** — `npm ci --legacy-peer-deps`
3. **Lint** — Currently skipped (can be enabled with ESLint)
4. **Test** — Run unit tests with code coverage (`ng test`)
5. **Build** — Production build with SSR (`ng build --configuration=production`)
6. **Static Code Analysis** — SonarQube scan of source code
7. **Build Docker Image** — Multi-stage Docker build (Node → nginx)
8. **Push Docker Image** — Push image to Docker Hub
9. **Deploy to Kubernetes** — Apply `k8s/deployment.yaml` and `k8s/service.yaml`, then roll out the new image

## Prerequisites

### Jenkins Configuration

1. **NodeJS Tool**
   - Install Node 20 plugin in Jenkins
   - Configure as `NodeJS-20` in Jenkins Global Tools

2. **Docker**
   - Jenkins agent must have Docker installed and running
   - Jenkins user must have Docker permissions: `usermod -aG docker jenkins`

3. **Credentials in Jenkins**
   - `sonarqube` (Secret text) — SonarQube authentication token
   - `docker-cred` (Username with password) — Docker Hub credentials
   - `docker-registry-url` (Secret text) — Docker registry URL (e.g., `dockerhubusername`)

### Local Setup (Developer Machine)

1. **Install sonar-scanner** (optional, for local analysis):
   ```bash
   npm install -g sonar-scanner
   ```

2. **Set SONAR_URL environment variable**:
   ```bash
   export SONAR_URL="http://localhost:9000"  # Adjust if running on different host
   ```

## Files Required

- `Jenkinsfile` — Pipeline configuration
- `Dockerfile` — Multi-stage Docker build
- `nginx.conf` — nginx server configuration for SPA routing
- `sonar-project.properties` — SonarQube project configuration
- `.dockerignore` — Files to exclude from Docker build context
- `k8s/deployment.yaml` — Kubernetes Deployment for the frontend
- `k8s/service.yaml` — Kubernetes NodePort Service for WSL/kubeadm access

## Jenkins Credentials Setup

### 1. Create SonarQube Token

1. Open SonarQube: `http://localhost:9000` (or your SonarQube instance)
2. Navigate to **My Account** → **Security**
3. Generate a token (e.g., `jenkins-token`)
4. Copy the token

### 2. Add Credentials to Jenkins

1. Go to Jenkins Dashboard → **Manage Jenkins** → **Credentials**
2. Click **Add Credentials**

**SonarQube Token:**
- Kind: `Secret text`
- Secret: `<paste the token>`
- ID: `sonarqube`
- Description: `SonarQube Jenkins Token`

**Docker Credentials:**
- Kind: `Username with password`
- Username: `<your-dockerhub-username>`
- Password: `<your-dockerhub-password-or-token>`
- ID: `docker-cred`
- Description: `Docker Hub Credentials`

**Docker Registry URL:**
- Kind: `Secret text`
- Secret: `<your-dockerhub-username>` (e.g., `mycompany` for `mycompany/jungle-frontend:123`)
- ID: `docker-registry-url`
- Description: `Docker Registry URL/Username`

## Environment Variables

The pipeline uses the following environment variables (configured in `Jenkinsfile`):

| Variable | Value | Purpose |
|----------|-------|---------|
| `CI` | `true` | Angular CI mode |
| `CHROME_BIN` | `/usr/bin/google-chrome-stable` | Karma test runner browser |
| `SONAR_URL` | `http://localhost:9000` | SonarQube server URL |
| `DOCKER_REGISTRY` | `credentials('docker-registry-url')` | Docker Hub username |
| `DOCKER_IMAGE` | `${DOCKER_REGISTRY}/jungle-frontend:${BUILD_NUMBER}` | Docker image name with tag |

### Updating SONAR_URL for Non-localhost

If running Jenkins in Docker and SonarQube on host:

```groovy
SONAR_URL = "http://172.21.34.22:9000"  // Use WSL IP, not localhost
```

Get your WSL IP with: `hostname -I`

## Running the Pipeline

### Trigger 1: Push to Repository
The pipeline is automatically triggered when code is pushed to the `DevOps_FrontEnd` branch (if webhook is configured).

### Trigger 2: Manual Build
1. Go to Jenkins Dashboard
2. Click on **Jungle-Frontend** job
3. Click **Build Now**

### View Build Logs
1. Click on the build number (e.g., `#123`)
2. Click **Console Output** to see real-time logs

## Coverage Report

Test coverage reports are published to Jenkins after the **Test** stage:

**Path:** `coverage/pi-front/index.html`

**Access:** 
1. In Jenkins build page, click **Coverage Report** link (under build artifacts)
2. Or navigate to: `http://jenkins:8080/job/Jungle-Frontend/<build-number>/Coverage_20Report/`

**Coverage Thresholds:**
- Statements: ~25% (current baseline)
- Branches: ~3%
- Functions: ~16%
- Lines: ~27%

Monitor and improve coverage over time.

## SonarQube Analysis

SonarQube results are available at: `http://localhost:9000/projects`

**Project Key:** `jungle-frontend`

**Configured Metrics:**
- Code coverage (from LCOV report)
- Code smells
- Bugs and vulnerabilities
- Duplications
- Type-safe TypeScript checks

### Troubleshooting SonarQube Stage

**Issue:** `Error: Inlining of fonts failed`
- **Solution:** Already fixed — fonts are consolidated in `src/styles.css`

**Issue:** `Cannot connect to SonarQube server`
- **Solution:** Check `SONAR_URL` is correct and SonarQube is running
- In Jenkins: `echo $SONAR_URL` to verify value
- Common fix for WSL: Use WSL IP (`172.21.x.x`) instead of `localhost`

**Issue:** `401 Unauthorized`
- **Solution:** Verify SonarQube token is correct and hasn't expired
- Regenerate token in SonarQube: **My Account** → **Security** → **Generate Token**

## Docker Build & Push

The pipeline builds a multi-stage Docker image:

**Build Stage:**
- Base image: `node:18-alpine`
- Installs dependencies: `npm ci --legacy-peer-deps`
- Builds Angular app: `npm run build -- --configuration production`

**Serve Stage:**
- Base image: `nginx:alpine`
- Copies dist to: `/usr/share/nginx/html`
- Uses custom `nginx.conf` for SPA routing

**Image Name:** `<registry>/jungle-frontend:${BUILD_NUMBER}`

Example: `mycompany/jungle-frontend:123`

## Kubernetes Deployment

After the Docker image is pushed, the Jenkins pipeline deploys the frontend to Kubernetes with:

1. `kubectl apply -f k8s/deployment.yaml`
2. `kubectl apply -f k8s/service.yaml`
3. `kubectl set image deployment/jungle-frontend app=${DOCKER_IMAGE}`
4. `kubectl rollout status deployment/jungle-frontend --timeout=3m`

### Local kubeadm / WSL Access

- The Service uses `NodePort` on `30080` so it can be reached from a Windows browser via `http://localhost:30080` when kubeadm is running inside WSL.
- Ensure the Jenkins agent has `kubectl` access to the cluster and a valid kubeconfig.

### Testing Docker Image Locally

```bash
# Build
docker build -t jungle-frontend:test .

# Run
docker run -p 8080:80 jungle-frontend:test

# Access: http://localhost:8080
```

## Troubleshooting

### Test Stage Fails
```bash
# Check Chrome installation
which google-chrome-stable

# Ensure Chrome is installed in Jenkins container/agent
# On Ubuntu/Debian:
apt-get install -y google-chrome-stable chromium-browser
```

### Build Stage Fails (Bundle Size)
- Check `angular.json` budgets are correctly relaxed for SSR
- Current budgets:
  - Initial: `1.5MB` warning / `2MB` error
  - Any component: `50kB` warning / `100kB` error

### Docker Build Fails
```bash
# Check Docker daemon is running
docker ps

# Check Docker permissions
groups jenkins  # Should include 'docker' group

# Manual build for debugging
docker build -t jungle-frontend:debug --progress=plain .
```

### Push Stage Fails
```bash
# Verify Docker credentials
docker login  # Use your Docker Hub credentials

# Check image exists locally
docker images | grep jungle-frontend

# Manual push for debugging
docker push <your-registry>/jungle-frontend:123
```

## Performance Optimization

### Build Time
- Current: ~12-15 seconds (production build)
- Caching: Docker layer caching for faster rebuilds
- Multi-stage build: Reduces final image size

### Image Size
- Compressed size: ~50-70 MB (nginx + angular app)
- Base nginx image: ~10 MB
- Built app: ~1.17 MB

### Coverage Report
- Only generated when tests pass
- Reports archived for historical tracking

## Best Practices

1. **Always use `npm ci` instead of `npm install`** — Ensures exact dependency versions
2. **Commit `.npmrc` with `legacy-peer-deps=true`** — Prevents peer dependency conflicts
3. **Keep sonar-project.properties updated** — Add exclusions when needed
4. **Monitor coverage trends** — Aim to maintain or improve coverage over time
5. **Tag Docker images semantically** — Consider `git commit hash` or `semantic versioning` instead of just `BUILD_NUMBER`
6. **Use image registries with retention policies** — Clean up old images to save storage

## Next Steps

1. Configure Jenkins webhook for automatic triggering on push
2. Set up deployment stage (Kubernetes, Docker Swarm, Docker Compose, etc.)
3. Add rollback mechanism for failed deployments
4. Implement notifications (Slack, email) on build success/failure
5. Add performance benchmarking stage
6. Configure automated dependency updates (Dependabot, Renovate)

## References

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Angular CLI Documentation](https://angular.io/cli)
- [Docker Documentation](https://docs.docker.com/)
- [nginx Documentation](https://nginx.org/en/docs/)

