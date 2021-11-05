rootProject.name = "kotlin-proto-dto-generator"

include("examples", "plugin")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
include("compiler")
