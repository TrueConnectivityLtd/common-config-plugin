@Library('utils') _

private boolean isBumpCommit() {
    lastCommit = sh([script: 'git log -1', returnStdout: true])
    if (lastCommit.contains("Setting version to")) {
        return true
    } else {
        return false
    }
}

pipeline {
    agent any

    environment {
        SBT_HOME = tool name: 'sbt-1.1.4', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
        PATH = "${env.SBT_HOME}/bin:${env.PATH}"
        ARTIFACTORY = credentials('artifactory-build')
        GIT_CREDENTIALS_FILE = '.git-credentials'
    }

    stages {
        stage('Checkout') {
           steps { 
            checkout scm
            abortPipelineWithPublishedVersion()

            script {
                if (isBumpCommit()) {
                    currentBuild.result = 'ABORTED'
                    error('Last commit bumped the version, aborting the build to prevent a loop.')
                }
            }
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
            environment {
                GITHUB = credentials('abff7286-8319-4696-be99-fcd161ffd78f')
            }
            steps {
                setupReleaseGithubUser username: "${GITHUB_USR}", password: "${GITHUB_PSW}", branch: "${BRANCH_NAME}", credentialsFile: "${GIT_CREDENTIALS_FILE}"
                sh '''
                    sbt "release with-defaults"
                '''
            }
        }
    }

    post {
        always {
            script {
                sh '''
                  rm -f ${GIT_CREDENTIALS_FILE}
                '''
            }
        }
    }
}
