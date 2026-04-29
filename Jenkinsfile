def runCommand(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

pipeline {
    agent any

    tools {
        nodejs 'NodeJS-20'
    }

    environment {
        CI = 'true'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install dependencies') {
            steps {
                script {
                    runCommand('npm ci --legacy-peer-deps')
                }
            }
        }

        stage('Lint') {
            steps {
                script {
                    def angularJson = readFile('angular.json')
                    if (angularJson.contains('"lint"')) {
                        runCommand('npx ng lint')
                    } else {
                        echo 'Skipping lint: no lint target is configured in angular.json.'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    runCommand('npx ng test --watch=false --browsers=ChromeHeadless --code-coverage --progress=false')
                }
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'coverage/PiFront',
                        reportFiles: 'index.html',
                        reportName: 'Coverage Report'
                    ])
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    runCommand('npx ng build --configuration=production')
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

