plugins {
    java
}

dependencies {
    implementation(project(":api"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}
