rootProject.name = "craftdsl"

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include("script")
include("host")
