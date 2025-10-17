## Gitlab Merge - Spring Boot Server (Java 8)

### 运行
```bash
mvn spring-boot:run
```

### 打包
```bash
mvn clean package
java -jar target/gitlab-merge-0.0.1-SNAPSHOT.jar
```

### 配置
编辑 `src/main/resources/application.yml` 中的 `server.port` 以及 `spring.redis.*`。


