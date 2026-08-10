plugins {
    java
}

dependencies {
    "shade"(project(":api"))
    "shade"(project(":text-adventure"))
    "shade"(project(":versions-adapter"))

    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

tasks.named<Jar>("jar") {
    destinationDirectory.set(file("..\\build"))
    archiveFileName.set("PhysicsControl.jar")
}
