# Backend code generator for spring boot


Usage:
```
$ git clone https://github.com/apuex/backend-gen
$ cd backend-gen
$ sbt spring_boot_codegen/assembly
$ java -Dproject.root=tmp -jar spring-boot-codegen/target/scala-2.12/spring-boot-codegen.jar generate-all spring-boot-codegen/src/main/resources/sample-model.xml
$ cd tmp/my-sample
$ mvn package
$ java -jar my-sample-app/target/my-sample-app-1.0-SNAPSHOT.jar
```
