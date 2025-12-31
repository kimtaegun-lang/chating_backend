pipeline {
    agent any
    
    environment {
        AWS_REGION = 'eu-north-1'
        AWS_ACCOUNT_ID = '663466149089'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        BACKEND_REPO = 'chating-backend'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        
        EC2_HOST_1 = '13.48.48.219'
        EC2_HOST_2 = '56.228.11.4'

        AWS_ACCESS_KEY_ID = credentials('aws-access-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        docker build -t ${BACKEND_REPO}:${IMAGE_TAG} .
                        docker tag ${BACKEND_REPO}:${IMAGE_TAG} ${ECR_REGISTRY}/${BACKEND_REPO}:${IMAGE_TAG}
                        docker tag ${BACKEND_REPO}:${IMAGE_TAG} ${ECR_REGISTRY}/${BACKEND_REPO}:latest
                    """
                }
            }
        }
        
        stage('Push to ECR') {
            steps {
                script {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        docker push ${ECR_REGISTRY}/${BACKEND_REPO}:${IMAGE_TAG}
                        docker push ${ECR_REGISTRY}/${BACKEND_REPO}:latest
                    """
                }
            }
        }
        
        stage('Deploy to EC2s') {
            steps {
                script {
                    def ec2Hosts = [EC2_HOST_1, EC2_HOST_2]
                    
                    for (host in ec2Hosts) {
                      sshagent(credentials: ['ec2-ssh-key']) {
    sh """
ssh -o StrictHostKeyChecking=no ec2-user@${host} << 'ENDSSH'
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
cd /home/ec2-user/app
docker-compose down || true
docker pull ${ECR_REGISTRY}/${BACKEND_REPO}:latest
docker-compose up -d
docker image prune -af || true
ENDSSH
"""
}

                        echo "Deployed to ${host}"
                        sleep 5
                    }
                }
            }
        }
        
     stage('Health Check') {
    steps {
        script {
            def ec2Hosts = [EC2_HOST_1, EC2_HOST_2]
            
            for (host in ec2Hosts) {
                
                def maxRetries = 12
                def retryDelay = 5
                def success = false
                
                for (int i = 1; i <= maxRetries; i++) {
                    try {
                        sshagent(credentials: ['ec2-ssh-key']) {
                            sh """
ssh -o StrictHostKeyChecking=no ec2-user@${host} << 'ENDSSH'
curl -f --connect-timeout 5 --max-time 10 http://localhost:8080/health
ENDSSH
"""
                        }
                        success = true
                        break
                    } catch (Exception e) {
                        if (i < maxRetries) {
                            echo "헬스 체크 재시도"
                            sleep retryDelay
                        } else {
                            error "헬스 체크 실패"
                        }
                    }
                }
            }
        }
    }
}
    }
    
 post {
    success {
        echo '백엔드 배포 성공!'
    }
    failure {
        echo '백엔드 배포 실패!'
    }
    always {
        script {
            sh 'docker image prune -f || true'
            cleanWs()
        }
    }
}
}