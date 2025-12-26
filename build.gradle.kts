plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
	// Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

	// lombok plugin
	implementation("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// test 환경
	testImplementation("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	// https://inpa.tistory.com/entry/IntelliJ-💽-Lombok-설치-방법-오류-해결 [Inpa Dev 👨‍💻:티스토리]

	// DB
	runtimeOnly("com.mysql:mysql-connector-j")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Redis
	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	// Lettuce (Spring Boot 기본 Redis 클라이언트)
	implementation("io.lettuce:lettuce-core")
	// Redis 분산락 (Redisson)
	implementation("org.redisson:redisson-spring-boot-starter:3.24.3")

	// Redis 모듈 추가
	// testImplementation("org.testcontainers:redis")  // ← 추가
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}
