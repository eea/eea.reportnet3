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
                failure {
                    slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'Build FAILED - Compilation Error in branch ' + env.BRANCH_NAME.replace('/', '_'), token: 'HRvukH8087RNW9NYQ3fd6jtM'
                }
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
                timeout(time: 2, unit: 'MINUTES') {
                    retry(3) {
                        script {
                            def props = readProperties  file: 'target/sonar/report-task.txt'
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
                                error  "Quality Gate Failure"
                            }
                            if ("WARN".equals(qualitygate["projectStatus"]["status"])) {
                                currentBuild.result = 'UNSTABLE'
                                slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'New Build Done - Quality Gate in WARNING (marked as UNSTABLE) https://sonar-oami.altia.es/dashboard?id=org.eea%3Areportnet%3A' + env.BRANCH_NAME.replace('/', '_') + '&did=1', token: 'HRvukH8087RNW9NYQ3fd6jtM'
                            }
                        }
                    }
                }
                
            }
            post {
                failure {
                    slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'New Build Done - Quality Gate NOT MET (marked as ERROR) https://sonar-oami.altia.es/dashboard?id=org.eea%3Areportnet%3A' + env.BRANCH_NAME.replace('/', '_') + '&did=1', token: 'HRvukH8087RNW9NYQ3fd6jtM'
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

        stage('Build Docker Images') {
            when {
                branch 'develop' 
            }
            steps {
                script {
                    echo 'Dataflow Service'
                    def app
                    app = docker.build("k8s-swi001:5000/dataflow-service:1.0", "--build-arg JAR_FILE=dataflow-service/target/dataflow-service-1.0-SNAPSHOT.jar --build-arg MS_PORT=8020 .")
                    app.push()                    
                }
                script {
                    echo 'Dataset Service'
                    def app
                    app = docker.build("k8s-swi001:5000/dataset-service:1.0", "--build-arg JAR_FILE=dataset-service/target/dataset-service-1.0-SNAPSHOT.jar --build-arg MS_PORT=8030 .")
                    app.push()                    
                }
                script {
                    echo 'API Gateway'
                    def app
                    app = docker.build("k8s-swi001:5000/api-gateway:1.0", "--build-arg JAR_FILE=api-gateway/target/api-gateway-1.0-SNAPSHOT.jar --build-arg MS_PORT=8010 .")
                    app.push()                    
                }
                script {
                    echo 'Inspire Harvester'
                    def app
                    app = docker.build("k8s-swi001:5000/inspire-harvester:3.0", "--build-arg JAR_FILE=inspire-harvester/target/inspire-harvester-3.0-SNAPSHOT.jar --build-arg MS_PORT=8050 .")
                    app.push()                    
                }
                script {
                    echo 'Recordstore Service'
                    def app
                    app = docker.build("k8s-swi001:5000/recordstore-service:3.0", "--build-arg JAR_FILE=recordstore-service/target/recordstore-service-3.0-SNAPSHOT.jar --build-arg MS_PORT=8090 .")
                    app.push()                    
                }
                script {
                    echo 'Validation Service'
                    def app
                    app = docker.build("k8s-swi001:5000/validation-service:1.0", "--build-arg JAR_FILE=validation-service/target/validation-service-1.0-SNAPSHOT.jar --build-arg MS_PORT=9000 .")
                    app.push()                    
                }
                script {
                    echo 'Collaboration Service'
                    def app
                    app = docker.build("k8s-swi001:5000/collaboration-service:3.0", "--build-arg JAR_FILE=collaboration-service/target/collaboration-service-3.0-SNAPSHOT.jar --build-arg MS_PORT=9010 .")
                    app.push()                    
                }
                script {
                    echo 'Communication Service'
                    def app
                    app = docker.build("k8s-swi001:5000/communication-service:3.0", "--build-arg JAR_FILE=communication-service/target/communication-service-3.0-SNAPSHOT.jar --build-arg MS_PORT=9020 .")
                    app.push()                    
                }
                script {
                    echo 'IndexSearch Service'
                    def app
                    app = docker.build("k8s-swi001:5000/indexsearch-service:3.0", "--build-arg JAR_FILE=indexsearch-service/target/indexsearch-service-3.0-SNAPSHOT.jar --build-arg MS_PORT=9030 .")
                    app.push()                    
                }
                script {
                    echo 'Document Container Service'
                    def app
                    app = docker.build("k8s-swi001:5000/document-container-service:3.0", "--build-arg JAR_FILE=document-container-service/target/document-container-service-3.0-SNAPSHOT.jar --build-arg MS_PORT=9030 .")
                    app.push()                    
                }
            }
        }
        
        
    }
}