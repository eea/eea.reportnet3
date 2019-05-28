pipeline {
    agent {
        label 'java8'
    }
    stages {
        stage('Preparation') {
            steps {
                sh 'echo "Starting CI/CD Pipeline"'
                sh 'ls -la /home/jenkins/.m2'
                sh 'env'
            }
        }
        stage('Compile') {
            steps {
                sh '''
                    mvn -Dmaven.test.failure.ignore=true -s '/home/jenkins/.m2/settings.xml' clean install
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
            
        }
        stage('Static Code Analysis') {
            steps {
                withSonarQubeEnv('Altia SonarQube') {
                    // requires SonarQube Scanner for Maven 3.2+
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.projectKey=org.eea:reportnet:' + env.BRANCH_NAME.replace('/', '_') + ' -Dsonar.projectName=ReportNet 3 - ' + env.BRANCH_NAME
                }
                slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'New Build Done - check quality here https://sonar-oami.altia.es/dashboard?id=org.eea%3Areportnet&did=1', token: 'HRvukH8087RNW9NYQ3fd6jtM'
            }
        }
        
        stage('Install in Nexus') {
            steps {
                sh '''
                    mvn -Dmaven.test.skip=true -s '/home/jenkins/.m2/settings.xml' deploy
                '''
            }
        }
        
        
    }
}