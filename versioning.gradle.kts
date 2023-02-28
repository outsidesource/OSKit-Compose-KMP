import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

data class SemVer(val major: Int = 0, val minor: Int = 0, val patch: Int = 0) : Comparable<SemVer> {
    override fun compareTo(other: SemVer): Int {
        if (this == other) return 0

        if (this.major > other.major) return 1
        if (this.major < other.major) return -1

        if (this.minor > other.minor) return 1
        if (this.minor < other.minor) return -1

        if (this.patch > other.patch) return 1
        if (this.patch < other.patch) return -1

        return 0
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    companion object {
        val Invalid = SemVer()

        fun fromString(version: String): SemVer {

            val versions = version.replace("[^0-9\\.]".toRegex(), "")
                .split(".")
                .map { it.take(1) }
                .filter { it.isNotBlank() }

            return SemVer(
                versions.getOrNull(0)?.toInt() ?: 0,
                versions.getOrNull(1)?.toInt() ?: 0,
                versions.getOrNull(2)?.toInt() ?: 0
            )
        }

        fun isValidSemVer(version: String): Boolean = version.matches("[0-9]+\\.[0-9]+\\.[0-9]+".toRegex())
    }
}


fun readVersionProps(): Properties {
    val versionFile = File(project.rootDir, "version.properties")
    var props = Properties()
    FileInputStream(versionFile).use { stream -> props.load(stream) }
    return props
}

fun readSemVerFromProps(props: Properties): SemVer = SemVer.fromString(props["version"].toString() ?: "0.0.0")

fun readVersionActual() {
    val version = readVersionProps()
    println("VERSION: ${version["version"]}")
}

fun incrementMajorVersion() {
    val versionFile = File(project.rootDir, "version.properties")
    val props = readVersionProps()
    val version = readSemVerFromProps(props)

    props.setProperty("version", version.copy(major = version.major+1, minor = 0, patch = 0).toString())
    FileOutputStream(versionFile).use { stream -> props.store(stream, null) }
    println("Major Version has been updated to: ${props["version"]}")
}

fun incrementMinorVersion() {
    val versionFile = File(project.rootDir, "version.properties")
    val props = readVersionProps()
    val version = readSemVerFromProps(props)

    props.setProperty("version", version.copy(minor = version.minor + 1, patch = 0).toString())
    FileOutputStream(versionFile).use { stream -> props.store(stream, null) }
    println("Major Version has been updated to: ${props["version"]}")
}

fun incrementPatchVersion() {
    val versionFile = File(project.rootDir, "version.properties")
    val props = readVersionProps()
    val version = readSemVerFromProps(props)

    props.setProperty("version", version.copy(patch = version.patch + 1).toString())
    FileOutputStream(versionFile).use { stream -> props.store(stream, null) }
    println("Major Version has been updated to: ${props["version"]}")
}

tasks.register("incrementMajorVersion") {
    doLast {
        incrementMajorVersion()
    }
}

tasks.register("incrementMinorVersion") {
    doLast {
        incrementMinorVersion()
    }
}

tasks.register("incrementPatchVersion") {
    doLast {
        incrementPatchVersion()
    }
}

tasks.register("readVersionActual") {
    doLast {
        readVersionActual()
    }
}