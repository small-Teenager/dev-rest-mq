#FROM openjdk:8-jre
# docker 中使用arthas 需要安装jdk https://arthas.aliyun.com/doc/docker.html
FROM openjdk:8-jdk
#作者
MAINTAINER yaodong199@icloud.com

#将宿主机jar包拷贝到容器中，此命令会将jar包拷贝到容器的根路径/下
ADD target/*.jar /application.jar

# 设置JVM初始堆内存为512M 设置JVM最大堆内存为256M 输出GC到指定路径下的文件中 出现FullGC时生成Heap转储文件 指定heap转储文件的存储路径
#ENV JAVA_OPTS="-Xmx512m -Xms256m -Xloggc:gc.log -XX:+HeapDumpBeforeFullGC -XX:HeapDumpPath=dump"

#容器启动时执行的命令
CMD ["java", "-jar" ,"/application.jar"]




