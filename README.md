# 技术点介绍
## 1、docker 打包镜像到虚拟机
- 虚拟机安装docker
- 为项目添加docker打包插件
    - 项目properties里面声明docker主机位置
    ```xml
       <properties>
              <java.version>1.8</java.version>
              <!--docker主机位置-->
              <dockerHost>http://192.168.159.128:2375</dockerHost>
        </properties>
    ```
    - 引入docker打包插件，放在build->plugins->plugin
    ```xml
       <!--docker打包插件-->
      <!--从环境变量中获取一个名为DOCKER_HOST  值为tcp://192.168.159.128:2375-->
      <plugin>
          <groupId>com.spotify</groupId>
          <artifactId>docker-maven-plugin</artifactId>
          <version>1.2.0</version>

          <configuration>
              <imageName>gmall/${project.build.finalName}</imageName>
              <!--这个jdk镜像稳定，容易下载来，官方的java以及openjdk都有问题 -->
              <baseImage>anapsix/alpine-java</baseImage>
              <!--<dockerDirectory>${basedir}/docker</dockerDirectory>  指定 Dockerfile 路径-->
              <entryPoint>["java","-jar","/${project.build.finalName}.jar"]</entryPoint>
              <resources>
                  <resource>
                      <targetPath>/</targetPath>
                      <directory>${project.build.directory}</directory>
                      <include>${project.build.finalName}.jar</include>
                  </resource>
              </resources>
          </configuration>
      </plugin>
    ```
    - 别忘记说明项目的名字 build->finalName
    ```xml
      <finalName>gmall-admin-web</finalName>
    ```
    - 别忘了，系统环境变量还要配置tcp
    ```java
      给系统classpath下配置
      DOCKER_HOST=tcp://192.168.159.128:2375
    ```
 - 虚拟机开放docker 的 2375端口
```shell
 vim /usr/lib/systemd/system/docker.service
 #给ExecStart=最后添加-H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock
 #重启docker；
 systemctl daemon-reload // 1，加载docker守护线程
 systemctl restart docker // 2，重启docker
```
 - 安装所有项目到仓库
 ```shell
  mvn clean install -Dmaven.test.skip=true
 ```
 
 - docker打包项目
 
  ```shell
    mvn clean package -Dmaven.test.skip=true docker:build
   ```
  
  - 运行镜像
  ```shell
    docker run -d -p 8081:8081 --name gmall-admin-web 94b3bd3aeb52
   ```
  


    

    