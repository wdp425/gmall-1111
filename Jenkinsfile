pipeline {
    agent any

    stages {
        stage('第二步,检测java环境是否正确') {
            steps {
                sh 'java -version'
                echo 'java环境正常可以使用..'
            }
        }
        stage('第三步，环境检查完成') {
            steps {
                echo '环境检查完毕'
            }
        }
    }
}