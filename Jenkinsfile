pipeline {
    agent any

    environment {
        SBT_HOME = tool name: 'sbt-1.1.4', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
        PATH = "${env.SBT_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Install Dependencies') {
            steps {
                sh 'sbt +update'
            }
        }
        stage('Build & Test') {
            steps {
                sh '''
                    sbt +test
                '''
            }
        }
        stage('Publish Snapshot') {
            when { not { branch 'develop' } }
            steps {
                sh '''
                    branch_quoted="${BRANCH_NAME/\\//}"
                    BUILD_VERSION=\"$BUILD_NUMBER-$branch_quoted\"
                    sbt "set version in ThisBuild := $BUILD_VERSION" publish
                '''
            } 
        }
        stage('Publish') {
            when { branch 'develop' }
            steps {
                sh 'sbt "+release with-defaults"'
            }
        }
    }
}
