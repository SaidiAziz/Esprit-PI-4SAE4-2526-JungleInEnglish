pipeline {
    agent any

    tools {
        nodejs 'NodeJS-20'
    }

    environment {
        CI           = 'true'
        CHROME_BIN   = '/usr/bin/google-chrome-stable'
        SONAR_URL    = "http://172.21.34.22:9000"
        DOCKER_IMAGE = "saidiaziz/jungle-frontend:${BUILD_NUMBER}"
        DOCKER_NAMESPACE     = 'saidiaziz'
        DOCKER_REPOSITORY    = 'jungle-frontend'
        DOCKER_KEEP_RELEASES = '3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm ci --legacy-peer-deps'
            }
        }

        stage('Lint') {
            steps {
                echo 'Lint not configured — skipping'
            }
        }

        stage('Test') {
            steps {
                sh '''
                    npx ng test \
                        --watch=false \
                        --browsers=ChromeHeadless \
                        --code-coverage \
                        --progress=false
                '''
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: true,
                        reportDir: 'coverage/pi-front',
                        reportFiles: 'index.html',
                        reportName: 'Coverage Report'
                    ])
                }
            }
        }

        stage('Build') {
            steps {
                sh 'npx ng build --configuration=production'
            }
        }

        stage('Static Code Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
                    sh '''
                        npx sonar-scanner \
                            -Dsonar.projectKey=jungle-frontend \
                            -Dsonar.projectName="Jungle Frontend" \
                            -Dsonar.sources=src \
                            -Dsonar.exclusions=**/node_modules/**,**/*.spec.ts \
                            -Dsonar.typescript.lcov.reportPaths=coverage/pi-front/lcov.info \
                            -Dsonar.host.url=${SONAR_URL} \
                            -Dsonar.login=${SONAR_AUTH_TOKEN}
                    '''
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build --no-cache -t ${DOCKER_IMAGE} .'
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE}
                        docker logout
                    '''
                }
            }
        }

        stage('Cleanup Old Docker Tags') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        node <<'NODE'
const https = require('https');

const namespace = process.env.DOCKER_NAMESPACE;
const repository = process.env.DOCKER_REPOSITORY;
const username = process.env.DOCKER_USER;
const password = process.env.DOCKER_PASS;
const keepCount = Number(process.env.DOCKER_KEEP_RELEASES || '3');
const currentTag = process.env.BUILD_NUMBER;

function request(method, path) {
  return new Promise((resolve, reject) => {
    const req = https.request({
      hostname: 'hub.docker.com',
      path,
      method,
      headers: {
        Authorization: `Basic ${Buffer.from(`${username}:${password}`).toString('base64')}`,
        'Content-Type': 'application/json'
      }
    }, res => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => resolve({ status: res.statusCode, body }));
    });
    req.on('error', reject);
    req.end();
  });
}

(async () => {
  try {
    let page = 1;
    const tags = [];

    while (true) {
      const response = await request('GET', `/v2/repositories/${encodeURIComponent(namespace)}/${encodeURIComponent(repository)}/tags?page_size=100&page=${page}`);
      if (response.status !== 200) {
        throw new Error(`Tag listing failed with HTTP ${response.status}: ${response.body}`);
      }

      const data = JSON.parse(response.body);
      tags.push(...(data.results || []));

      if (!data.next) {
        break;
      }

      page += 1;
    }

    const numericTags = tags
      .filter(tag => /^\d+$/.test(tag.name))
      .sort((a, b) => new Date(b.last_updated) - new Date(a.last_updated));

    const protectedTags = new Set([currentTag, 'latest']);
    const retainedNumeric = [];

    for (const tag of numericTags) {
      if (tag.name === currentTag) {
        continue;
      }

      if (retainedNumeric.length < Math.max(keepCount - 1, 0)) {
        protectedTags.add(tag.name);
        retainedNumeric.push(tag.name);
      }
    }

    const deletions = numericTags.filter(tag => !protectedTags.has(tag.name));

    for (const tag of deletions) {
      const response = await request('DELETE', `/v2/repositories/${encodeURIComponent(namespace)}/${encodeURIComponent(repository)}/tags/${encodeURIComponent(tag.name)}/`);
      if (response.status !== 200 && response.status !== 202 && response.status !== 204) {
        throw new Error(`Failed to delete tag ${tag.name}: HTTP ${response.status} ${response.body}`);
      }
      console.log(`Deleted old Docker tag: ${tag.name}`);
    }

    console.log(`Retained ${Math.min(keepCount, numericTags.length)} numeric release tags.`);
  } catch (error) {
    console.log(`Docker tag cleanup skipped: ${error.message}`);
  }
})();
NODE
                        docker image prune -f || true
                        docker rmi ${DOCKER_IMAGE} || true
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo "Pipeline failed — branch ${env.BRANCH_NAME}, build #${env.BUILD_NUMBER}"
        }
        always {
            cleanWs()
        }
    }
}
