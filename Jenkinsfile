 pipeline {
    agent any

    environment {
        AWS_REGION   = 'ap-northeast-2'
        ECR_REPO     = 'nemonemo-backend'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        BACKEND_USER = 'ubuntu'
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

                                # ECR 로그인 (IAM Role 사용)
                                aws ecr get-login-password --region ${AWS_REGION} | \\
                                    docker login --username AWS --password-stdin ${ECR_REGISTRY}

                                # 새 이미지 Pull
                                docker pull ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}

                                # 기존 컨테이너 교체
                                docker stop nemonemo-backend 2>/dev/null || true
                                docker rm   nemonemo-backend 2>/dev/null || true

                                # 배포
                                docker run -d \\
                                    --name nemonemo-backend \\
                                    -p 8080:8080 \\
                                    --env-file /etc/nemonemo/prod.env \\
                                    --restart unless-stopped \\
                                    ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}

                                # 이전 이미지 정리
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
