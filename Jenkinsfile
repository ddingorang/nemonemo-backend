 pipeline {
    agent any

    environment {
        AWS_REGION   = 'ap-northeast-2'
        ECR_REPO     = 'nemonemo'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        BACKEND_USER = 'ec2-user'
        CONTAINER_NAME = 'nemonemo'
    }

    triggers {
        githubPush()
    }

    stages {

        // ──────────────────────────────────────────────
        // 0. Checkout
        // ──────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ──────────────────────────────────────────────
        // 1. Build & Test
        // ──────────────────────────────────────────────
        stage('Build & Test') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build --no-daemon'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
                    sh 'rm -rf build/libs/*.jar'
                }
            }
        }

        // ──────────────────────────────────────────────
        // 2. Docker Build & ECR Push
        // ──────────────────────────────────────────────
        stage('Docker Build & ECR Push') {
            steps {
                withCredentials([
                    string(credentialsId: 'ECR_REGISTRY', variable: 'ECR_REGISTRY'),
                    [$class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'AWS_CREDENTIALS',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']
                ]) {
                    script {
                        def fullImage = "${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
                        def latestImage = "${ECR_REGISTRY}/${ECR_REPO}:latest"

                        sh """
                            docker build -t ${fullImage} .
                            docker tag ${fullImage} ${latestImage}

                            aws ecr get-login-password --region ${AWS_REGION} | \
                                docker login --username AWS --password-stdin ${ECR_REGISTRY}

                            docker push ${fullImage}
                            docker push ${latestImage}

                            docker rmi ${fullImage} ${latestImage} || true
                        """
                    }
                }
            }
        }

        // ──────────────────────────────────────────────
        // 3. Deploy to Backend Server
        // ──────────────────────────────────────────────
        // 3. Deploy to Backend Server
                stage('Deploy') {
                    steps {
                        withCredentials([
                            string(credentialsId: 'ECR_REGISTRY', variable: 'ECR_REGISTRY'),
                            string(credentialsId: 'BACKEND_HOST', variable: 'BACKEND_HOST')
                        ]) {
                            sshagent(['SSH_KEY']) {
                                sh """
                                    ssh -o StrictHostKeyChecking=no ${BACKEND_USER}@${BACKEND_HOST} '
                                        set -e

                                        aws ecr get-login-password --region ${AWS_REGION} | \\
                                            docker login --username AWS --password-stdin ${ECR_REGISTRY}

                                        docker pull ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}

                                        # 컨테이너 이름 변수 사용
                                        docker stop ${CONTAINER_NAME} 2>/dev/null || true
                                        docker rm   ${CONTAINER_NAME} 2>/dev/null || true

                                        docker run -d \\
                                            --name ${CONTAINER_NAME} \\
                                            -p 8080:8080 \\
                                            --env-file /etc/nemonemo/prod.env \\
                                            --restart unless-stopped \\
                                            ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}

                                        docker image prune -f
                                    '
                                """
                            }
                        }
                    }
                }
    }

    post {
        success {
            echo "배포 성공: ${ECR_REPO}:${IMAGE_TAG}"
        }
        failure {
            echo "파이프라인 실패 — build #${env.BUILD_NUMBER} 확인 필요"
        }
    }
}
