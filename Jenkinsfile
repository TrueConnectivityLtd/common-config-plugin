pipeline {
    agent any

    environment {
        SBT_HOME = tool name: 'sbt-1.1.4', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
        PATH = "${env.SBT_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Install Dependencies') {
            steps {
                sh 'sbt update'
            }
        }
        stage('Build & Test') {
            steps {
                sh '''
                    sbt test
                '''
            }
        }
        stage('Publish Snapshot') {
            when { not { branch 'develop' } }
            environment {
                ARTIFACTORY = credentials('publishSettings.sbt')
            }
            steps {
                // TODO move this to library
                sh '''
                    pwd
                    echo $ARTIFACTORY
                '''
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
            environment {
                ARTIFACTORY = credentials('publishSettings.sbt')
            }
            steps {
                sh '''
                    pwd
                    echo $ARTIFACTORY
                '''
                sh '''
                    ./gitconfig.sh
                    sbt "release with-defaults"
                '''
            }
        }
    }

    post {
        always {
            script {
                sh 'sh "git config --local --remove-section credential"'
            }
        }
    }
}
