pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build & Test Backend') {
            parallel {
                stage('AI Learning Assistant') {
                    steps {
                        dir('backend-integre/AI-Learning-Assistant-Microservice') {
                            sh 'chmod +x mvnw && ./mvnw -B clean package -DskipTests'
                        }
                    }
                }
                stage('Collaboration Room') {
                    steps {
                        dir('backend-integre/Collaboration-Room-Microservice') {
                            sh 'chmod +x mvnw && ./mvnw -B clean package -DskipTests'
                        }
                    }
                }
                stage('User Microservice') {
                    steps {
                        dir('backend-integre/User-Microservice') {
                            sh 'chmod +x mvnw && ./mvnw -B clean package -DskipTests'
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        dir('backend-integre/api-gateway') {
                            sh 'chmod +x mvnw && ./mvnw -B clean package -DskipTests'
                        }
                    }
                }
                stage('Eureka Server') {
                    steps {
                        dir('backend-integre/eureka-server') {
                            sh 'chmod +x mvnw && ./mvnw -B clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Docker Build') {
            parallel {
                stage('ai-learning-assistant') {
                    steps {
                        sh 'docker build -t jungle/ai-learning-assistant:ci backend-integre/AI-Learning-Assistant-Microservice'
                    }
                }
                stage('collaboration-room') {
                    steps {
                        sh 'docker build -t jungle/collaboration-room:ci backend-integre/Collaboration-Room-Microservice'
                    }
                }
                stage('user-microservice') {
                    steps {
                        sh 'docker build -t jungle/user-microservice:ci backend-integre/User-Microservice'
                    }
                }
                stage('api-gateway') {
                    steps {
                        sh 'docker build -t jungle/api-gateway:ci backend-integre/api-gateway'
                    }
                }
                stage('eureka-server') {
                    steps {
                        sh 'docker build -t jungle/eureka-server:ci backend-integre/eureka-server'
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline réussi ✅'
        }
        failure {
            echo 'Pipeline échoué ❌'
        }
    }
}