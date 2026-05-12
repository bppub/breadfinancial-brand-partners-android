//------------------------------------------------------------------------------
//  File:          PlacementData.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.core.models

/**
 * Data model representing placement-related configuration
 */
data class PlacementData(
    var financingType: BreadPartnersFinancingType? = null,
    var locationType: BreadPartnersLocationType? = null,
    var placementId: String? = null,
    var domID: String? = null,
    var allowCheckout: Boolean? = null,
    var order: Order? = null,
    var defaultSelectedCardKey: String? = null,
    var selectedCardKey: String? = null,
    val upqInSessionToken: String? = null,
    val financingBuyerId: String? = null,
    val prequalificationId: String? = null,
    val prequalCreditLimit: String? = null
)

/**
 * Enum representing different placement location types used for configuring
 * where Bread Partners UI elements appear within the host app.
 */
enum class BreadPartnersLocationType(val value: String) {
    BAG("bag"), BANNER("banner"), CART("cart"), CATEGORY("category"), CHECKOUT("checkout"), DASHBOARD(
        "dashboard"
    ),
    FOOTER("footer"), HOMEPAGE("homepage"), LANDING("landing"), LOYALTY("loyalty"), MOBILE("mobile"), PRODUCT(
        "product"
    ),
    HEADER("header"), SEARCH("search"), MYACCOUNT("myaccount");

    companion object {
        val locationChannelMap: Map<BreadPartnersLocationType, String> = mapOf(
            HOMEPAGE to "H",
            LANDING to "L",
            SEARCH to "S",
            PRODUCT to "P",
            CATEGORY to "C",
            BANNER to "U",
            CHECKOUT to "O",
            CART to "A",
            MOBILE to "E",
            LOYALTY to "D",
            FOOTER to "F",
            BAG to "2",
            DASHBOARD to "5",
            MYACCOUNT to "5",
            HEADER to "R"
        )
    }

    fun getChannelCode(): String? = locationChannelMap[this]
}

/**
 * Enum representing different financial types used for placement configurations.
 */
enum class BreadPartnersFinancingType(val value: String) {
    CARD("card"), INSTALLMENTS("installments"), VERSATILE("versatile")
}

/**
 * Data model representing order information.
 */
data class Order(
    var subTotal: CurrencyValue? = null,
    var totalDiscounts: CurrencyValue? = null,
    var totalPrice: CurrencyValue? = null,
    var totalShipping: CurrencyValue? = null,
    var totalTax: CurrencyValue? = null,
    var discountCode: String? = null,
    var pickupInformation: PickupInformation? = null,
    var fulfillmentType: OrderFulfillmentType? = null,
    var items: List<Item>? = null,
    var bnplEligible: Boolean? = null
)

/**
 * Data model representing currency value information.
 */
data class CurrencyValue(
    var currency: String? = null, var value: Long? = null
)

/**
 * Data model representing pickup information.
 */
data class PickupInformation(
    var name: Name? = null,
    var phone: String? = null,
    var address: BreadPartnersAddress? = null,
    var email: String? = null
)


/**
 * Data model representing pickup information.
 */
data class Name(
    var givenName: String? = null,
    var familyName: String? = null,
    var additionalName: String? = null
)

/**
 * Data model representing cart item information.
 */
data class Item(
    var name: String? = null,
    var category: String? = null,
    var quantity: Int? = null,
    var unitPrice: CurrencyValue? = null,
    var unitTax: CurrencyValue? = null,
    var sku: String? = null,
    var itemUrl: String? = null,
    var imageUrl: String? = null,
    var description: String? = null,
    var shippingCost: CurrencyValue? = null,
    var shippingProvider: String? = null,
    var shippingDescription: String? = null,
    var shippingTrackingNumber: String? = null,
    var shippingTrackingUrl: String? = null,
    var fulfillmentType: ItemFulfillmentType? = null
)

/**
 * Enum representing different fulfillment types for orders.
 */
enum class OrderFulfillmentType(val value: String) {
    PICKUP("PICKUP"),
    DELIVERY("DELIVERY"),
    MULTIPLE("MULTIPLE"),
    UNKNOWN("UNKNOWN")
}

/**
 * Enum representing different fulfillment types for Item.
 */
enum class ItemFulfillmentType(val value: String) {
    PICKUP("PICKUP"),
    DELIVERY("DELIVERY"),
    UNKNOWN("UNKNOWN")
}


/**
 * Set of ineligible item categories for BNPL
 */
val INELIGIBLE_ITEM_CATEGORIES = setOf("non-leasable", "nonleasable")



