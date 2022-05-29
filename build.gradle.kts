plugins {
	application
	idea
	java
	kotlin("jvm") version "1.6.10"
}

// Note: define javaVersion, javaMajorVersion, javaHomeLinux, javaHomeMac, and javaHomeWindows
//       inside your gradle.properties file
val javaVersion: String by project
val javaMajorVersion: String by project
val kotlinTargetJdk: String by project
val javaHomeLinux: String by project
val javaHomeMac: String by project
val javaHomeWindows: String by project

subprojects {
	ext {
		set("javaVersion", javaVersion)
		set("javaMajorVersion", javaMajorVersion)
		set("kotlinTargetJdk", kotlinTargetJdk)
	}
}

application {
	mainClassName = "com.projectswg.holocore.ProjectSWG"
}

repositories {
	maven("https://dev.joshlarson.me/maven2")
	mavenCentral()
	maven("https://jitpack.io")	// Automatically creates a JVM library based on a git repository
}

sourceSets {
	main {
		dependencies {
			implementation(project(":pswgcommon"))
			implementation(kotlin("stdlib"))
			
			implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
			
			implementation(group="org.xerial", name="sqlite-jdbc", version="3.30.1")
			implementation(group="org.mariadb.jdbc", name="mariadb-java-client", version="2.5.4")
			implementation(group="org.mongodb", name="mongodb-driver-sync", version="3.12.2")
			implementation(group="me.joshlarson", name="fast-json", version="3.0.1")
			implementation(group="me.joshlarson", name="jlcommon-network", version="1.1.0")
			implementation(group="com.github.madsboddum", name="swgterrain", version="1.1.3")
		}
	}
	test {
		dependencies {
			implementation(group="junit", name="junit", version="4.13.2")
			implementation(group="org.mockito", name="mockito-core", version="3.8.0")
		}
	}
	create("utility")
}

val utilityImplementation by configurations.getting {
	extendsFrom(configurations.implementation.get())
}

dependencies {
	val holocoreProject = project(":")
    val pswgcommonProject = project(":pswgcommon")

    utilityImplementation(holocoreProject)
    utilityImplementation(pswgcommonProject)
	utilityImplementation(group="org.jetbrains.kotlin", name="kotlin-stdlib", version="1.3.50")
	utilityImplementation(group="org.xerial", name="sqlite-jdbc", version="3.23.1")
	utilityImplementation(group="org.mongodb", name="mongodb-driver-sync", version="3.12.2")
	utilityImplementation(group="me.joshlarson", name="fast-json", version="3.0.0")
}

idea {
	targetVersion = javaMajorVersion
    module {
        inheritOutputDirs = true
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
	kotlinOptions {
		jvmTarget = kotlinTargetJdk
	}
	destinationDir = sourceSets.main.get().java.outputDir
}

tasks.create<JavaExec>("runDebug") {
	enableAssertions = true
	classpath = sourceSets.main.get().runtimeClasspath
	main = "com.projectswg.holocore.ProjectSWG"
}
