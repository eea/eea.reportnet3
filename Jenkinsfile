String cron_working_branch = BRANCH_NAME != "develop" ? "@hourly" : ""

pipeline {

    triggers {
        cron(cron_working_branch)
    }

    agent {
        label 'java8'
    }
    stages {
        stage('Preparation') {
            steps {
                sh 'echo "Starting CI/CD Pipeline"'                
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
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -P sonar -Dsonar.jenkins.branch=' + env.BRANCH_NAME.replace('/', '_')
                }
                slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'New Build Done - check quality here https://sonar-oami.altia.es/dashboard?id=org.eea%3Areportnet%3A' + env.BRANCH_NAME.replace('/', '_') + '&did=1', token: 'HRvukH8087RNW9NYQ3fd6jtM'
            }
        }
        
        stage('Install in Nexus') {
            when {
                branch 'develop' 
            }
            steps {
                sh '''
                    mvn -Dmaven.test.skip=true -s '/home/jenkins/.m2/settings.xml' deploy
                '''
            }
        }

        stage('Push to EEA GitHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'eea-github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eea/eea.reportnet3.git')
                }
            }
        }
        
        
    }
}