@Library('utils')
import com.trueconnectivity.internal.Constants

pipeline {
    agent any

    environment {
        SBT_HOME = tool name: 'sbt-1', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
        PATH = "${env.SBT_HOME}/bin:${env.PATH}"
        ARTIFACTORY = credentials('artifactory-build')
    }

    stages {
        stage('Checkout') {
            steps {
                abortPipelineWithPublishedVersion()
            }
        }
        stage('Build & Test') {
            steps {
                sh 'sbt coverage test'
            }
        }
        stage('Publish Snapshot') {
            when { not { branch 'master' } }
            steps {
                sbtPublishSnapshot()
            } 
        }
        stage('Publish') {
            when { branch 'master' }
            steps {
                sbtPublishRelease()
            }
        }
        stage('SonarQube Analysis') {
            when { environment name: 'CHANGE_ID', value: '' } // when not in PR build
            steps {
                withSonarQubeEnv('Main') {
                    script {
                        sh 'sbt coverageReport'
                        sh "${tool 'Scanner 4.3'}/bin/sonar-scanner -Dsonar.projectVersion=\$(cat version.sbt | cut -d '\"' -f2) -Dsonar.links.scm=${scm.getUserRemoteConfigs()[0].getUrl()} -Dsonar.links.ci=${env.BUILD_URL}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                sh """
                    rm -f ${Constants.GIT_CREDENTIALS_FILE}
                """
            }
        }
    }
}
