@Library('utils')
import com.trueconnectivity.internal.Constants

pipeline {
    agent any

    environment {
        SBT_HOME = tool name: 'sbt-1.1.4', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
        PATH = "${env.SBT_HOME}/bin:${env.PATH}"
        ARTIFACTORY = credentials('artifactory-build')
        CREDENTIALS_FILE = "${Constants.GIT_CREDENTIALS_FILE}"
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
            when { not { branch 'develop' } }
            steps {
                sh '''
                    branch_quoted="${BRANCH_NAME/\\//}"
                    BUILD_VERSION=$BUILD_NUMBER-$branch_quoted
                    sed -i -e "s/\\"$/.$BUILD_VERSION\\"/" version.sbt 
                    sbt publish
                '''
            } 
        }
        stage('Publish') {
            when { branch 'develop' }
            steps {
                sbtPublishRelease()
            }
        }
    }

    post {
        always {
            script {
                sh '''
                    rm -f $CREDENTIALS_FILE
                '''
            }
        }
    }
}
