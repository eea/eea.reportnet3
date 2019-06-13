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
            }
        }

        stage("Quality Gate"){
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    retry(3) {
                        script {
                            sh "${scannerHome}/bin/sonar-scanner"
                            sh "cat .scannerwork/report-task.txt"
                            def props = readProperties  file: '.scannerwork/report-task.txt'
                            echo "properties=${props}"
                            def sonarServerUrl=props['serverUrl']
                            def ceTaskUrl= props['ceTaskUrl']
                            def ceTask
                            timeout(time: 1, unit: 'MINUTES') {
                                waitUntil {
                                    def response = httpRequest ceTaskUrl
                                    ceTask = readJSON text: response.content
                                    echo ceTask.toString()
                                    return "SUCCESS".equals(ceTask["task"]["status"])
                                }
                            }
                            def response2 = httpRequest url : sonarServerUrl + "/api/qualitygates/project_status?analysisId=" + ceTask["task"]["analysisId"], authentication: 'jenkins_scanner'
                            def qualitygate =  readJSON text: response2.content
                            echo qualitygate.toString()
                            if ("ERROR".equals(qualitygate["projectStatus"]["status"])) {
                                error  "Quality Gate failure"
                            }
                        }
                    }
                }
                post {
                    failure {
                        slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'New Build Done - Quality Gate not mer https://sonar-oami.altia.es/dashboard?id=org.eea%3Areportnet%3A' + env.BRANCH_NAME.replace('/', '_') + '&did=1', token: 'HRvukH8087RNW9NYQ3fd6jtM'
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