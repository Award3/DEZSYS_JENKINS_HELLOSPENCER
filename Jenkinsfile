pipeline {
    agent any
    stages {
        stage('Source') {
            steps {
                checkout scm
                echo 'Code ausgecheckt'
            }
        }
        stage('Build') {
            steps {
                echo 'Build läuft...'
                // z.B.: sh 'mvn clean package' für Maven
                // oder: sh './gradlew build' für Gradle
            }
        }
        stage('Test') {
            steps {
                echo 'Tests laufen...'
                // z.B.: sh 'mvn test'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deployment...'
            }
        }
    }
}
