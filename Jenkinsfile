String cron_working_branch = BRANCH_NAME != "develop" ? "@daily" : ""

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

        stage("Quality Gate"){
            steps {
                timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
                    def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
                    if (qg.status != 'OK') {
                        slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'New Build Done - Quality Gate not mer https://sonar-oami.altia.es/dashboard?id=org.eea%3Areportnet%3A' + env.BRANCH_NAME.replace('/', '_') + '&did=1', token: 'HRvukH8087RNW9NYQ3fd6jtM'
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"

                    }
                }
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
            when {
                branch 'develop' 
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'eea-github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh('git config --global user.email "jorge.saenz@altia.es"')
                    sh('git config --global user.name "Jorge Sáenz (ALTIA)"')
                    sh('git pull https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eea/eea.reportnet3.git develop --allow-unrelated-histories')
                    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eea/eea.reportnet3.git HEAD:develop')
                }
            }
        }
        
        
    }
}