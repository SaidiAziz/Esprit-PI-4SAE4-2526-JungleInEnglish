pipeline {
    agent any

    tools {
        nodejs 'NodeJS-20'
    }

    environment {
        CI         = 'true'
        CHROME_BIN = '/usr/bin/google-chrome-stable'
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

