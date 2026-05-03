pipeline {
    agent any

    tools {
        nodejs 'NodeJS-20'
    }

    environment {
        CI              = 'true'
        CHROME_BIN      = '/usr/bin/google-chrome-stable'
        SONAR_URL       = "http://localhost:9000"
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_IMAGE    = "${DOCKER_REGISTRY}/jungle-frontend:${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install dependencies') {
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
                sh 'docker build -t ${DOCKER_IMAGE} .'
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE}
                        docker logout
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

