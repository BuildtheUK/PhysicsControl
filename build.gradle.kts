plugins {
    java
}

allprojects {
    group = "org.btuk.pcontrol"
    version = "2.0.0"

    val javaVersion = 25

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
    }

    plugins.withType<JavaPlugin> {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(javaVersion))
            }
        }
    }

    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven { url = uri("https://repo.aikar.co/nexus/content/repositories/aikar") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    configurations {
        val shade = create("shade")
        plugins.withType<JavaPlugin> {
            configurations.getByName("implementation").extendsFrom(shade)
        }
    }
}

subprojects {
    apply(plugin = "java")

    tasks.named<Jar>("jar") {
        val shade = configurations.getByName("shade")
        from(shade.map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}
