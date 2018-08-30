# Backend code generator for spring boot

## Prerequisites
- jdk 1.8
- sbt - to build this project
- maven
- [Google Protobuf](https://github.com/google/protobuf)

Usage:
```
$ git clone https://github.com/apuex/spring-boot-solution
$ cd spring-boot-solution
$ sbt codegen/assembly
$ java -Dproject.root=tmp \
       -Dsymbol.naming=microsoft \
       -jar codegen/target/scala-2.12/codegen.jar generate-all codegen/src/main/resources/sample-model.xml
$ cd tmp/my-sample
$ mvn package
$ java -jar my-sample-app/target/my-sample-app-1.0-SNAPSHOT.jar
```
