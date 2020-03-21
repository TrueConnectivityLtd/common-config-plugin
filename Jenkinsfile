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
                sh 'sbt test'
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
