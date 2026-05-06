pipeline {
    agent any

    options {
        skipDefaultCheckout(false)
    }

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

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-prod', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        export KUBECONFIG="$KUBECONFIG_FILE"
                        echo "Testing cluster connection..."
                        kubectl cluster-info

                        echo "Deploying to Kubernetes..."
                        kubectl apply -f k8s/deployment.yaml
                        kubectl apply -f k8s/service.yaml
                        kubectl set image deployment/jungle-frontend app=${DOCKER_IMAGE}
                        kubectl rollout status deployment/jungle-frontend --timeout=3m
                        echo "✓ Deployment successful"
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
