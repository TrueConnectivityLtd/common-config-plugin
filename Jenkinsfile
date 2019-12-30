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
                ARTIFACTORY = credentials('artifactory')
            }
            steps {
                sh '''
                    cp $ARTIFACTORY publishSettings.sbt
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
                ARTIFACTORY = credentials('artifactory')
                GITHUB = credentials('abff7286-8319-4696-be99-fcd161ffd78f')
            }
            steps {
                sh '''
                    cp $ARTIFACTORY publishSettings.sbt
                    echo "https://${GITHUB_USR}:${GITHUB_PSW}@github.com"> .git-credentials
                    git config --local credential.username $GITHUB_USR
                    git config --local credential.helper 'store --file=.git-credentials'
                    git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
                    git config branch.${BRANCH_NAME}.remote origin
                    git config branch.${BRANCH_NAME}.merge refs/heads/${BRANCH_NAME}
                    sbt "release with-defaults"
                '''
            }
        }
    }

    post {
        always {
            script {
                sh '''
                  rm -f publishSettings.sbt
                  rm -f .git-credentials
                '''
            }
        }
    }
}
