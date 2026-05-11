package com.breadfinancial.breadpartners.sdk.core.models

data class UnifiedPrequalPathResult(
    val path: String,
    val queryString: String,
    val queryParams: Map<String, Any?>
)

/**
 * Data class representing address information for UPQ
 */
data class UPQAddressRequest(
    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null
)
