package com.adobe.aem_kmp_boilerplate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform