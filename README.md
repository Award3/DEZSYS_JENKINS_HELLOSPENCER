# Jenkins CI/CD Pipeline – Schritt-für-Schritt Guide

## Übersicht

Dieses Dokument führt dich durch die komplette Einrichtung einer Jenkins CI/CD Pipeline mit Docker-Deployment. Am Ende hast du eine Pipeline, die automatisch bei einem Git-Commit baut, testet und deployed.

---

## Teil 1: Jenkins installieren & starten

### 1.1 Jenkins Container starten

```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:latest
```

- `-p 8080:8080` = Jenkins Web-UI
- `-p 50000:50000` = Jenkins Agent Port
- `-v jenkins_home:/var/jenkins_home` = Daten persistent speichern
  ![[Pasted image 20260512110840.png]]
### 1.2 Jenkins freischalten

1. Initial-Passwort auslesen:

    ```bash
    docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
    ```

2. Browser öffnen: `http://localhost:8080`
3. Passwort eingeben
4. **Install suggested plugins** wählen
5. Admin-User anlegen (Benutzername, Passwort, E-Mail merken)
   ![[Pasted image 20260512111000.png]]
---
![[Pasted image 20260512111301.png]]
## Teil 2: Erste einfache Pipeline ("Hello World")

### 2.1 Pipeline anlegen

1. Jenkins Dashboard → **New Item**
2. Name: `HelloWorld-Pipeline`
3. Typ: **Pipeline** → OK

### 2.2 Pipeline Script eintragen

Unter **Pipeline → Definition** → **Pipeline script** wählen und einfügen:

```groovy
pipeline {
    agent any
    stages {
        stage('Source') {
            steps {
                echo 'Stage: Source – Code wird geholt'
            }
        }
        stage('Build') {
            steps {
                echo 'Stage: Build – Projekt wird gebaut'
            }
        }
        stage('Test') {
            steps {
                echo 'Stage: Test – Tests werden ausgeführt'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Hello World! Deployment erfolgreich!'
            }
        }
    }
}
```

### 2.3 Testen

1. **Save** → **Build Now**
2. Unter **Build History** auf den Build klicken → **Console Output**
3. Du solltest alle 4 Stages mit ihren Echo-Ausgaben sehen
   ![[Pasted image 20260512111450.png]]
   Output:
   Started by user Nico Gal

[Pipeline] Start of Pipeline
[Pipeline] node
Running on Jenkins
in /var/jenkins_home/workspace/`HelloWorld-Pipeline`
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Source)
[Pipeline] echo
Stage: Source – Code wird geholt
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Build)
[Pipeline] echo
Stage: Build – Projekt wird gebaut
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Test)
[Pipeline] echo
Stage: Test – Tests werden ausgeführt
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Deploy)
[Pipeline] echo
Hello World! Deployment erfolgreich!
[Pipeline] }
[Pipeline] // stage
[Pipeline] }
[Pipeline] // node
[Pipeline] End of Pipeline
Finished: SUCCESS

---

## Teil 3: GitHub Repository einbinden

### 3.1 Neues Pipeline-Projekt anlegen

1. **New Item** → Name: `GitHub-Pipeline` → Typ: **Pipeline**
2. Unter **Pipeline → Definition**: **Pipeline script from SCM** wählen
3. SCM: **Git**
4. Repository URL: die URL deines GitHub-Repos eintragen (z.B. `https://github.com/ThomasMicheler/DEZSYS_JENKINS_HELLOSPENCER.git` als Vorlage, oder dein eigenes Repo)
5. Branch: `*/main`
6. Script Path: `Jenkinsfile` (Standard)
7. **Save**

### 3.2 Jenkinsfile im Repo anlegen

Falls noch nicht vorhanden, erstelle im Root deines Repos ein `Jenkinsfile`:

```groovy
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
```

### 3.3 Testen

**Build Now** klicken → Jenkins clont das Repo und führt die Pipeline aus.

---

## Teil 4: Docker in Jenkins einrichten

### 4.1 Jenkins Plugins installieren

1. **Manage Jenkins** → **Plugins** → **Available plugins**
2. Suche und installiere:
    - **Docker plugin**
    - **CloudBees Docker Build and Publish** (bzw. "Docker Pipeline")
3. **Restart Jenkins** nach der Installation (Checkbox "Restart Jenkins when installation is complete")

### 4.2 Jenkins Container stoppen & mit Docker-Socket neu starten

```bash
docker stop jenkins
docker rm jenkins

docker run -u root -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:latest
```

**Wichtig:** `-v /var/run/docker.sock:/var/run/docker.sock` gibt Jenkins Zugriff auf den Docker-Daemon des Host-Systems.

### 4.3 Docker CLI im Jenkins Container installieren

```bash
docker exec -it jenkins bash
```

Im Container:

```bash
apt-get update
apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release

curl -fsSL https://download.docker.com/linux/debian/gpg | \
  gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
  https://download.docker.com/linux/debian $(lsb_release -cs) stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce-cli
```

### 4.4 Docker testen

Noch im Jenkins Container:

```bash
docker --version
docker ps
```

Wenn beides funktioniert → `exit` und weiter.

---

## Teil 5: Java-Applikation mit Unit Tests

> Erfüllt: _Erstellung der Unit-Tests für diese Applikation_

### 5.1 Beispiel-Projektstruktur (Maven)

```
my-app/
├── Jenkinsfile
├── Dockerfile
├── pom.xml
└── src/
    ├── main/java/com/example/
    │   └── App.java
    └── test/java/com/example/
        └── AppTest.java
```

### 5.2 Beispiel: App.java

```java
package com.example;

public class App {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }

    public int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        App app = new App();
        System.out.println(app.greet("Spencer"));
    }
}
```

### 5.3 Beispiel: AppTest.java (JUnit 5)

```java
package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void testGreet() {
        App app = new App();
        assertEquals("Hello, Spencer!", app.greet("Spencer"));
    }

    @Test
    void testGreetEmpty() {
        App app = new App();
        assertEquals("Hello, !", app.greet(""));
    }

    @Test
    void testAdd() {
        App app = new App();
        assertEquals(5, app.add(2, 3));
    }

    @Test
    void testAddNegative() {
        App app = new App();
        assertEquals(-1, app.add(2, -3));
    }
}
```

### 5.4 pom.xml (Minimalversion)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

## Teil 6: Dockerfile für das Deployment

### 6.1 Dockerfile

Im Root des Projekts:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/my-app-1.0-SNAPSHOT.jar /app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## Teil 7: Vollständige Jenkins Pipeline (Jenkinsfile)

```groovy
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
```

- Maven im Container installieren (`apt-get install -y maven`), oder

---

## Teil 8: GitHub Webhook (Automatischer Build bei Commit)

### 8.1 Jenkins-Seite konfigurieren

1. Pipeline-Projekt öffnen → **Configure**
2. Unter **Build Triggers**: **GitHub hook trigger for GITScm polling** aktivieren
3. **Save**

### 8.2 GitHub Webhook einrichten

1. GitHub Repo → **Settings** → **Webhooks** → **Add webhook**
2. Payload URL: `http://<deine-ip>:8080/github-webhook/`
3. Content type: `application/json`
4. Events: **Just the push event**
5. **Add webhook**

**Problem: localhost ist nicht erreichbar von GitHub.** Lösungen:

- **ngrok** verwenden: `ngrok http 8080` → die generierte URL als Webhook-URL verwenden
  ![[Pasted image 20260512131727.png]]
### 8.3 Testen

1. Mach einen Commit & Push auf dein Repo
2. Jenkins sollte automatisch einen neuen Build starten

---

## Teil 9: Überprüfung mit curl

Nach dem Deployment kannst du die laufende App testen:

```bash
# Prüfen ob der Container läuft
docker ps | grep my-app-container

# Falls die App einen HTTP-Endpoint hat
curl http://localhost:8081
```
![[Pasted image 20260512133725.png]]
![[Pasted image 20260512133815.png]]
