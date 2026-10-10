//------------------------------------------------------------------------------
//  File:          UnifiedPrequalTest.kt
//  Author(s):     Bread Financial
//  Date:          15 July 2026
//
//  Descriptions:  Unit tests for the UnifiedPrequalPathResult and
//  UPQAddressRequest data classes.
//
//  © 2026 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk

import com.breadfinancial.breadpartners.sdk.core.models.UPQAddressRequest
import com.breadfinancial.breadpartners.sdk.core.models.UnifiedPrequalPathResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedPrequalPathResultTest {

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Test
    fun `Stores all properties as provided`() {
        val params: Map<String, Any?> = mapOf("locationType" to "checkout", "count" to 3)
        val result = UnifiedPrequalPathResult(
            path = "/prequalify",
            queryString = "locationType=checkout&count=3",
            queryParams = params
        )

        assertEquals("/prequalify", result.path)
        assertEquals("locationType=checkout&count=3", result.queryString)
        assertEquals(2, result.queryParams.size)
        assertEquals("checkout", result.queryParams["locationType"] as? String)
        assertEquals(3, result.queryParams["count"] as? Int)
    }

    @Test
    fun `Supports empty path and query string`() {
        val result = UnifiedPrequalPathResult(
            path = "",
            queryString = "",
            queryParams = emptyMap()
        )

        assertTrue(result.path.isEmpty())
        assertTrue(result.queryString.isEmpty())
        assertTrue(result.queryParams.isEmpty())
    }

    @Test
    fun `Preserves null values inside queryParams`() {
        val params: Map<String, Any?> = mapOf("optional" to null, "present" to "value")
        val result = UnifiedPrequalPathResult(
            path = "/path",
            queryString = "present=value",
            queryParams = params
        )

        // Key exists but its value is null.
        assertTrue(result.queryParams.keys.contains("optional"))
        assertNull(result.queryParams["optional"])
        assertEquals("value", result.queryParams["present"] as? String)
    }
}

class UPQAddressRequestTest {

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Test
    fun `Stores all address fields when fully populated`() {
        val address = UPQAddressRequest(
            address1 = "123 Main St",
            address2 = "Apt 4B",
            city = "Columbus",
            state = "OH",
            zip = "43004"
        )

        assertEquals("123 Main St", address.address1)
        assertEquals("Apt 4B", address.address2)
        assertEquals("Columbus", address.city)
        assertEquals("OH", address.state)
        assertEquals("43004", address.zip)
    }

    @Test
    fun `Supports null for every field`() {
        val address = UPQAddressRequest(
            address1 = null,
            address2 = null,
            city = null,
            state = null,
            zip = null
        )

        assertNull(address.address1)
        assertNull(address.address2)
        assertNull(address.city)
        assertNull(address.state)
        assertNull(address.zip)
    }

    @Test
    fun `Defaults every field to null when no arguments provided`() {
        val address = UPQAddressRequest()

        assertNull(address.address1)
        assertNull(address.address2)
        assertNull(address.city)
        assertNull(address.state)
        assertNull(address.zip)
    }
}