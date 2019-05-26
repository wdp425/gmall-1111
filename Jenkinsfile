pipeline {
    agent any

    stages {
        stage('第一步,检测mvn环境是否正确') {
                    steps {
                        sh 'mvn -v'
                        echo 'java环境正常可以使用..'
                        sh 'mvn clean package -Dmaven.test.skip=true docker:build'
                    }
         }
        stage('第二步,启动各个提供者') {
            steps {
                sh 'java -version'
                echo 'java环境正常可以使用..'
                sh 'docker run -d -p 8080:8080 --name gmall-order 1bc3'
                sh 'docker run -d -p 8080:8080 --name gmall-user 1bc3'
                sh 'docker run -d -p 8080:8080 --name gmall-cart 1bc3'
            }
        }
        stage('第三步，启动各个消费者') {
            steps {
                echo '环境检查完毕'
                 sh 'docker run -d -p 8080:8080 --name gmall-order 1bc3'
                 sh 'docker run -d -p 8080:8080 --name gmall-user 1bc3'
                 sh 'docker run -d -p 8080:8080 --name gmall-cart 1bc3'
            }
        }
    }
}