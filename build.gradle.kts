plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "cahayakurnia"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
//	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

// postgresql
	implementation("org.postgresql:postgresql:42.7.3")

	implementation("org.postgresql:postgresql")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Optional: untuk debugging dan development
	runtimeOnly("com.h2database:h2") // untuk testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Jakarta Persistence API
	implementation("jakarta.persistence:jakarta.persistence-api")

	// Supabase Java Client
	implementation("io.github.jan-tennert.supabase:postgrest-kt:1.4.7")
	implementation("io.github.jan-tennert.supabase:storage-kt:1.4.7")
	implementation("io.github.jan-tennert.supabase:gotrue-kt:1.4.7")

	// File upload handling
	implementation("commons-fileupload:commons-fileupload:1.5")
	implementation("commons-io:commons-io:2.11.0")
	
	// .env file support
	implementation("me.paulschwarz:spring-dotenv:4.0.0")
}



tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootRun {
	jvmArgs("-Dspring.profiles.active=prod")
	// atau systemProperty("spring.profiles.active","prod")
}
