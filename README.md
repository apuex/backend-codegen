# Backend code generator for spring boot


Usage:
```
$ git clone https://github.com/apuex/backend-gen
$ cd backend-gen
$ sbt spring_boot_codegen/assembly
$ java -jar spring-boot-codegen/target/scala-2.12/spring-boot-codegen.jar generate-dao spring-boot-codegen/src/main/resources/sample-model.xml
```
