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
 - 安装所有项目到仓库，
 ```shell
  #在gmall-parent执行以下命令
  mvn clean install -Dmaven.test.skip=true
 ```
 
 - docker打包项目
 
  ```shell
    #在需要打包的项目执行以下命令
    mvn clean package -Dmaven.test.skip=true docker:build
   ```
  
  - 运行镜像
  ```shell
    docker run -d -p 8081:8081 --name gmall-admin-web 94b3bd3aeb52
   ```
  

# docker自定义Dockerfile构建自定义镜像
   - 准备资源 .jar .tar.gz  ，将Dockerfile和资源放在一起
   - 编写Dockerfile文件
   ```dockerfile
    # 基础镜像的名字
    FROM centos
    
    # 维护者信息
    MAINTAINER leifengyangc@163.com
    
    # 将准备好的tar.gz这个压缩包，解压到当前自己容器的 /usr/local/src目录下
    ADD nginx-1.14.2.tar.gz /usr/local/src
    
    # 运行我们需要的一些命令,docker在构建镜像的时候会自动运行这些命令，来改变当前容器里面的一些内容
    RUN yum install -y gcc gcc-c++ glibc make autoconf openssl openssl-devel
    RUN yum install -y libxslt-devel -y gd gd-devel GeoIP GeoIP-devel pcre pcre-devel
    RUN useradd -M -s /sbin/nologin nginx
    
    # 相当于cd 到之前解压好的目录位置；  cd /usr/local/src/nginx-1.14.2
    WORKDIR /usr/local/src/nginx-1.14.2
    
    # 配置nginx 然后make和make install
    RUN ./configure --user=nginx --group=nginx --prefix=/usr/local/nginx --with-file-aio --with-http_ssl_module --with-http_realip_module --with-http_addition_module --with-http_xslt_module --with-http_image_filter_module --with-http_geoip_module --with-http_sub_module --with-http_dav_module --with-http_flv_module --with-http_mp4_module --with-http_gunzip_module --with-http_gzip_static_module --with-http_auth_request_module --with-http_random_index_module --with-http_secure_link_module --with-http_degradation_module --with-http_stub_status_module && make && make install
    
    # 暴露出当前容器的80端口
    EXPOSE 80

   ```
   - 使用docker依据上次编写好的dockerfile构建我们的镜像
   ```shell
     docker build -t 镜像名:版本号  .(这个点是表示去哪里找到Dockerfile)
   ```
   
## 我们推荐每个项目自己写上Dockerfile文件即可；


    

    