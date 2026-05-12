pipeline {
    agent any

    stages {
        stage('Source') {
            steps {
                checkout scm
                echo 'Repository geklont'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
                echo 'Build erfolgreich'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
                echo 'Alle Tests bestanden'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t my-app:latest .'
                echo 'Docker Image erstellt'
            }
        }

        stage('Deploy') {
            steps {
                // Alten Container stoppen falls vorhanden
                sh 'docker stop my-app-container || true'
                sh 'docker rm my-app-container || true'

                // Neuen Container starten
                sh 'docker run -d --name my-app-container -p 8081:8081 my-app:latest'
                echo 'Deployment abgeschlossen – App läuft auf Port 8081'
            }
        }
    }

    post {
        success {
            echo 'Pipeline erfolgreich abgeschlossen!'
        }
        failure {
            echo 'Pipeline fehlgeschlagen – Logs prüfen.'
        }
    }
}