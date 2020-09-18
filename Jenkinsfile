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
        stage('Setup sandbox docker images build'){
            steps{
                script {
                  if (env.BRANCH_NAME == 'sandbox' || env.BRANCH_NAME == "feature/SPRINT_22_FE_AllowAccess" ) {
                      env.TAG_SUFIX="_sandbox"
                  } else {
                     env.TAG_SUFIX=""
                  }
                }
            }
        }
        stage('Build Docker Images') {
            when {
                expression {
                   return BRANCH_NAME == "develop" || BRANCH_NAME == "sandbox" || BRANCH_NAME == "feature/SPRINT_22_FE_AllowAccess"
                }
            }
            parallel {





                stage('Build Frontend') {
                    steps {
                        script {
                            echo 'ReportNet 3.0 Frontend'
                            def app
                            app = docker.build("k8s-swi001:5000/reportnet-frontend-service:3.0$TAG_SUFIX", "--no-cache  ./frontend-service/")
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
