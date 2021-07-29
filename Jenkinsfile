String cron_working_branch = BRANCH_NAME != "develop" && BRANCH_NAME != "sandbox" ? "@daily" : ""

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
            parallel {
                stage('Compile JAVA') {
                    steps {
                        sh '''
                            mvn -Dmaven.test.failure.ignore=true -s '/home/jenkins/.m2/settings.xml' clean install

                        '''

                    }
                    post {
                        failure {
                            slackSend baseUrl: 'https://altia-alicante.slack.com/services/hooks/jenkins-ci/', channel: 'reportnet3', message: 'Build FAILED - JAVA Compilation Error in branch ' + env.BRANCH_NAME.replace('/', '_'), token: 'HRvukH8087RNW9NYQ3fd6jtM'
                        }
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                
                /*stage('Compile NPM') {
                    steps {
                    	sh 'rm -rf /node_modules/'
                        sh 'rm -f package-lock.json'
                        sh '''
                            npm install --no-cache frontend-service/
                        '''
                    }

                }*/
            }
        }
       

        stage('Install in Nexus') {
            when {
                branch 'develop1'
            }
            parallel {
                stage('Install in JAVA repository') {
                    steps {
                        sh '''
                            mvn -Dmaven.test.skip=true -s '/home/jenkins/.m2/settings.xml' deploy
                        '''
                    }
                }
                stage('Install in NPM repository') {
                    steps {
                        sh '''
                            npm publish frontend-service/ --registry=https://nexus-oami.altia.es/content/repositories/npm-internal/
                        '''
                    }
                }
            }
        }

        stage('Push to EEA GitHub') {
            when {
                branch 'sandbox'
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'jenkins-eea-altia', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh('git config --global user.email "ext.jose.luis.anton@altia.es"')
                    sh('git config --global user.name "Jose Luis Anton (ALTIA)"')
                    sh('git pull https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eea/eea.reportnet3.git sandbox --allow-unrelated-histories')
                    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eea/eea.reportnet3.git HEAD:sandbox')
                }
            }
        }
        stage('Setup sandbox docker images build'){
            steps{
                script {
                  if (env.BRANCH_NAME == 'sandbox') {
                      env.TAG_SUFIX="_sandbox"
                  } else {
                     env.TAG_SUFIX=""
                  }
                   env.DATAFLOW_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=dataflow.version -q -DforceStdout', returnStdout: true
                   env.DATASET_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=dataset.version -q -DforceStdout', returnStdout: true
                   env.RECORDSTORE_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=recordstore.version -q -DforceStdout', returnStdout: true
                   env.VALIDATION_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=validation.version -q -DforceStdout', returnStdout: true
                   env.COLLABORATION_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=collaboration.version -q -DforceStdout', returnStdout: true
                   env.DOCUMENT_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=document.version -q -DforceStdout', returnStdout: true
                   env.API_GATEWAY_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=api-gateway.version -q -DforceStdout', returnStdout: true
                   env.INSPIRE_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=inspire.version -q -DforceStdout', returnStdout: true
                   env.COMMUNICATION_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=communication.version -q -DforceStdout', returnStdout: true
                   env.INDEXSEARCH_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=indexsearch.version -q -DforceStdout', returnStdout: true
                   env.UMS_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=ums.version -q -DforceStdout', returnStdout: true
                   env.FRONTEND_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=frontend.version -q -DforceStdout', returnStdout: true
                   env.ROD_VERSION = sh script: 'mvn -f $WORKSPACE/parent-poms/parent/pom.xml  help:evaluate -Dexpression=rod.version -q -DforceStdout', returnStdout: true


                }
            }
        }
        stage('Build Docker Images') {
            when {
                expression {
                   return BRANCH_NAME == "develop" 
                }
            }
            parallel {
                stage('Build Core Platform') {
                    steps {
                        script {
                            echo 'Dataflow Service'
                            def app
                            app = docker.build("k8s-swi001:5000/dataflow-service:1.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/dataflow-service-" + env.DATAFLOW_VERSION + ".jar --build-arg MS_PORT=8020 -f ./Dockerfile ./dataflow-service")
                            app.push()

                        }
                        script {
                            echo 'Dataset Service'
                            def app
                            app = docker.build("k8s-swi001:5000/dataset-service:1.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/dataset-service-" + env.DATAFLOW_VERSION + ".jar --build-arg MS_PORT=8030 -f ./Dockerfile ./dataset-service")
                            app.push()

                        }
                        script {
                            echo 'Recordstore Service'
                            def app
                            app = docker.build("k8s-swi001:5000/recordstore-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/recordstore-service-" + env.RECORDSTORE_VERSION + ".jar --build-arg MS_PORT=8090 ./recordstore-service/")
                            app.push()

                        }
                        script {
                            echo 'Validation Service'
                            def app
                            app = docker.build("k8s-swi001:5000/validation-service:1.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/validation-service-" + env.VALIDATION_VERSION + ".jar --build-arg MS_PORT=8015 -f ./Dockerfile ./validation-service")
                            app.push()

                        }
                        script {
                            echo 'Collaboration Service'
                            def app
                            app = docker.build("k8s-swi001:5000/collaboration-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/collaboration-service-" + env.COLLABORATION_VERSION + ".jar --build-arg MS_PORT=9010 -f ./Dockerfile ./collaboration-service")
                            app.push()

                        }
                        script {
                            echo 'Document Container Service'
                            def app
                            app = docker.build("k8s-swi001:5000/document-container-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/document-container-service-" + env.DOCUMENT_VERSION + ".jar --build-arg MS_PORT=9040 -f ./Dockerfile ./document-container-service")
                            app.push()

                        }

                    }
                 }

                stage('Build Integration Layer') {
                  steps{
                        script {
                            echo 'API Gateway'
                            def app
                            app = docker.build("k8s-swi001:5000/api-gateway:1.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/api-gateway-" + env.API_GATEWAY_VERSION + ".jar --build-arg MS_PORT=8010 -f ./Dockerfile ./api-gateway ")
                            app.push()

                        }
                        script {
                            echo 'Inspire Harvester'
                            def app
                            app = docker.build("k8s-swi001:5000/inspire-harvester:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/inspire-harvester-" + env.INSPIRE_VERSION + ".jar --build-arg MS_PORT=8050 -f ./Dockerfile ./inspire-harvester ")
                            app.push()

                        }
                         script {
                            echo 'Communication Service'
                            def app
                            app = docker.build("k8s-swi001:5000/communication-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/communication-service-" + env.COMMUNICATION_VERSION + ".jar --build-arg MS_PORT=9020 -f ./Dockerfile ./communication-service")
                            app.push()

                         }
                        script {
                            echo 'IndexSearch Service'
                            def app
                            app = docker.build("k8s-swi001:5000/indexsearch-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/indexsearch-service-" + env.INDEXSEARCH_VERSION + ".jar --build-arg MS_PORT=9030 -f ./Dockerfile ./indexsearch-service")
                            app.push()

                        }
                        script {
                            echo 'User Management Service'
                            def app
                            app = docker.build("k8s-swi001:5000/user-management-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/user-management-service-" + env.UMS_VERSION + ".jar --build-arg MS_PORT=9010 -f ./Dockerfile ./user-management-service")
                            app.push()

                        }
                        script {
                            echo 'ROD Service'
                            def app
                            app = docker.build("k8s-swi001:5000/rod-service:3.0" + env.TAG_SUFIX, "--build-arg JAR_FILE=target/rod-service-" + env.ROD_VERSION + ".jar --build-arg MS_PORT=9050 -f ./Dockerfile ./rod-service")
                            app.push()

                        }


                  }
                }


                stage('Build Frontend') {
                    steps {
                        script {
                            echo 'ReportNet 3.0 Frontend'
                            def app
                            app = docker.build("k8s-swi001:5000/reportnet-frontend-service:3.0" + env.TAG_SUFIX, " ./frontend-service/")
                            app.push()                    

                        }
                    }
                }

            }
        }
        stage('Cleaning docker images'){
          when {
            expression {
              return BRANCH_NAME == "develop" || BRANCH_NAME == "sandbox"
            }
          }
          steps {
            script {
              sh 'docker rmi k8s-swi001:5000/api-gateway:1.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/dataflow-service:1.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/dataset-service:1.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/recordstore-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/validation-service:1.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/collaboration-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/document-container-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/inspire-harvester:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/communication-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/indexsearch-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/user-management-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/rod-service:3.0${TAG_SUFIX}'
              sh 'docker rmi k8s-swi001:5000/reportnet-frontend-service:3.0${TAG_SUFIX}'
            }
          }
        }


    }
}

