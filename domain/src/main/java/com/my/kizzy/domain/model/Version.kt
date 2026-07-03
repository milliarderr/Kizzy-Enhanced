// https://github.com/Ashinch/ReadYou/blob/main/app/src/main/java/me/ash/reader/data/model/general/Version.kt

package com.my.kizzy.domain.model

import com.my.kizzy.domain.model.release.Release

/**
 * A class that represents a version.
 * @param numbers The version numbers.
 * @property major The major version number.
 * @property minor The minor version number.
 */
class Version(numbers: List<String>) {

    private var major: Int = 0
    private var minor: Int = 0

    init {
        major = numbers.getOrNull(0)?.toIntOrNull() ?: 0
        minor = numbers.getOrNull(1)?.toIntOrNull() ?: 0
    }

    constructor() : this(listOf())
    constructor(string: String?) : this(string?.split(".") ?: listOf())

    override fun toString() = "$major.$minor"

    /**
     * Use [major], [minor] for comparison.
     *
     * 1. [major] <=> [other.major]
     * 2. [minor] <=> [other.minor]
     */
    operator fun compareTo(other: Version): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    /**
     * Returns whether this version is larger [current] version.
     */
    fun whetherNeedUpdate(current: Version): Boolean = this > current
}

// Extract trailing build number for both "6.2-enhanced-13" and "enhanced-13"
private fun String?.buildNumber(): Int? = this?.substringAfterLast("-")?.toIntOrNull()

fun String?.toVersion(): Version =
    buildNumber()?.let { Version(listOf("0", it.toString())) } ?: Version(this)

fun Release.toVersion(): Version =
    tagName.buildNumber()?.let { Version(listOf("0", it.toString())) } ?: Version(tagName)