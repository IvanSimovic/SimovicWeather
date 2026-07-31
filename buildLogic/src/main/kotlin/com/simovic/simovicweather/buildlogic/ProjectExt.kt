package com.simovic.simovicweather.buildlogic

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

val Project.versions: LibrariesForLibs.VersionAccessors
    get() = libs.versions
