pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Tests Unitaires') {
            steps {
                sh 'mvn test'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=quiz-service \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                        -Dsonar.junit.reportPaths=target/surefire-reports
                    '''
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t maaouisamar/quiz-service .'
            }
        }
        stage('Push Docker Hub') {
            steps {
                sh '''
                    echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                    docker push maaouisamar/quiz-service
                '''
            }
        }
    }
    post {
        success {
            echo '✅ Pipeline CI quiz-service réussi !'
        }
        failure {
            echo '❌ Pipeline CI quiz-service échoué !'
        }
    }
}