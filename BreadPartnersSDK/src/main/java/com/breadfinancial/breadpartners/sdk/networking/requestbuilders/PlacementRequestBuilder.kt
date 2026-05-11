//------------------------------------------------------------------------------
//  File:          PlacementRequestBuilder.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartners.sdk.networking.requestbuilders

import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersBuyer
import com.breadfinancial.breadpartners.sdk.core.models.INELIGIBLE_ITEM_CATEGORIES
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration.PaymentMode
import com.breadfinancial.breadpartners.sdk.core.models.Order
import com.breadfinancial.breadpartners.sdk.core.models.PlacementData
import com.breadfinancial.breadpartners.sdk.core.models.UPQAddressRequest
import com.breadfinancial.breadpartners.sdk.core.models.UnifiedPrequalPathResult
import com.breadfinancial.breadpartners.sdk.networking.models.ContextRequestBody
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementRequest
import com.breadfinancial.breadpartners.sdk.networking.models.PlacementRequestBody
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnersExtensions.takeIfNotEmpty
import com.breadfinancial.breadpartners.sdk.utilities.CommonUtils
import com.breadfinancial.breadpartners.sdk.utilities.toQueryString
import com.google.gson.Gson

/**
 * Builder class to create a PlacementRequest for fetching placements.
 */
class PlacementRequestBuilder(
    integrationKey: String,
    merchantConfiguration: MerchantConfiguration?,
    placementData: PlacementData?,
) {
    private var placements: MutableList<PlacementRequestBody> = mutableListOf()
    private var brandId: String = integrationKey

    init {
        createPlacementRequestBody(merchantConfiguration, placementData)
    }

    private fun createPlacementRequestBody(
        merchantConfiguration: MerchantConfiguration?,
        placementData: PlacementData?,
    ) {
        var context = ContextRequestBody(
            ENV = merchantConfiguration?.env?.value.takeIfNotEmpty(),
            PRICE = placementData?.order?.totalPrice?.value,
            CARDHOLDER_TIER = merchantConfiguration?.cardholderTier.takeIfNotEmpty(),
            STORE_NUMBER = merchantConfiguration?.storeNumber.takeIfNotEmpty(),
            LOYALTY_ID = merchantConfiguration?.loyaltyID.takeIfNotEmpty(),
            OVERRIDE_KEY = merchantConfiguration?.overrideKey.takeIfNotEmpty(),
            CLIENT_VAR_1 = merchantConfiguration?.clientVariable1.takeIfNotEmpty(),
            CLIENT_VAR_2 = merchantConfiguration?.clientVariable2.takeIfNotEmpty(),
            CLIENT_VAR_3 = merchantConfiguration?.clientVariable3.takeIfNotEmpty(),
            CLIENT_VAR_4 = merchantConfiguration?.clientVariable4.takeIfNotEmpty(),
            DEPARTMENT_ID = merchantConfiguration?.departmentId.takeIfNotEmpty(),
            channel = merchantConfiguration?.channel
                ?: placementData?.locationType?.getChannelCode() ?: "X",
            subchannel = merchantConfiguration?.subchannel ?: "X",
            CMP = merchantConfiguration?.campaignID.takeIfNotEmpty(),
            ALLOW_CHECKOUT = placementData?.allowCheckout ?: false,
            LOCATION = placementData?.locationType?.value.takeIfNotEmpty(),
        )

        context = if (placementData?.allowCheckout == true) {
            val upqCheckoutData = mapUnifiedPlacementContextToUpqCheckout(
                placementData = placementData,
                merchantConfiguration = merchantConfiguration,
            )

            val upqPathData = pathForUnifiedPrequalCheckout(
                initialData = upqCheckoutData,
                clientKey = brandId
            ).queryString

            context.copy(UPQ_CHECKOUT_PARAMS = upqPathData)
        } else {
            val upqData = mapUnifiedPlacementContextToUPQCommonData(
                placementData = placementData,
                merchantConfiguration = merchantConfiguration
            )
            val upqPathData = pathForUnifiedPrequal(
                initialData = upqData,
                clientKey = brandId
            ).queryString

            context.copy(UPQ_PARAMS = upqPathData)
        }

        val placement = PlacementRequestBody(
            context = context,
            id = placementData?.placementId.takeIfNotEmpty()
        )

        placements.add(placement)
    }

    fun build(): PlacementRequest {
        return PlacementRequest(
            brandId = brandId,
            placements = placements,
        )
    }


    /**
     * Maps unified placement context to UPQ prequalification checkout data.
     * Includes all common data fields plus checkout-specific fields.
     *
     * @param placementConfig Unified placement configuration (optional)
     * @param setupConfig Unified setup configuration (optional)
     * @param sessionTrackingId Session tracking identifier (optional)
     * @param userTrackingId User tracking identifier (optional)
     * @param prequalLimit Prequalification credit limit (optional)
     * @param prequalId Prequalification ID (optional)
     * @param financingBuyerId Financing buyer ID (optional)
     * @param financingLocationId Financing location ID (optional)
     * @param callCenter Call center identifier (optional)
     * @param inSessionToken In-session token (optional)
     * @return MutableMap with all mapped checkout data
     */
    fun mapUnifiedPlacementContextToUpqCheckout(
        placementData: PlacementData? = null,
        merchantConfiguration: MerchantConfiguration? = null,
        sessionTrackingId: String? = null,
        userTrackingId: String? = null,
        financingLocationId: String? = null,
        callCenter: String? = null,

        ): MutableMap<String, Any?> {
        // Map common data from placement and setup configs
        val commonData = mapUnifiedPlacementContextToUPQCommonData(
            placementData = placementData,
            merchantConfiguration = merchantConfiguration,
            sessionId = sessionTrackingId,
            userTrackingId = userTrackingId
        )

        // Map order and check BNPL eligibility
        val newOrder = mapUnifiedPlacementOrderToOrder(placementData?.order)
        checkBnplEligibility(newOrder)

        // Map shipping address
        val shippingAddress =
            mapUnifiedPlacementContextToUPQAddressRequest(merchantConfiguration?.buyer)

        // Merge all data using assignDefined
        return CommonUtils().assignDefined(
            mutableMapOf<String, Any?>(),
            commonData,
            mapOf(
                "order" to newOrder,
                "shippingAddress" to shippingAddress,
                "prequalCreditLimit" to placementData?.prequalCreditLimit,
                "prequalificationId" to placementData?.prequalificationId,
                "financingBuyerId" to placementData?.financingBuyerId,
                "financingLocationId" to financingLocationId,
                "callCenter" to callCenter,
                "inSessionToken" to placementData?.upqInSessionToken
            )
        )
    }

    /**
     * Maps unified placement context to UPQ common data.
     * Transforms all fields from placementConfig and setupConfig to CommonData format.
     *
     * @param placementConfig Unified placement configuration (optional)
     * @param setupConfig Unified setup configuration (optional)
     * @param sessionId Session tracking identifier (optional)
     * @param userTrackingId User tracking identifier (optional)
     * @return CommonData with mapped fields from both configs
     */
    fun mapUnifiedPlacementContextToUPQCommonData(
        placementData: PlacementData? = null,
        merchantConfiguration: MerchantConfiguration? = null,
        sessionId: String? = null,
        userTrackingId: String? = null
    ): MutableMap<String, Any?> {
        return CommonUtils().assignDefined(
            mutableMapOf<String, Any?>(),
            mapOf(
                "firstName" to merchantConfiguration?.buyer?.givenName,
                "lastName" to merchantConfiguration?.buyer?.familyName,
                "address1" to merchantConfiguration?.buyer?.billingAddress?.address1,
                "address2" to merchantConfiguration?.buyer?.billingAddress?.address2,
                "city" to merchantConfiguration?.buyer?.billingAddress?.locality,
                "state" to merchantConfiguration?.buyer?.billingAddress?.region,
                "zip" to merchantConfiguration?.buyer?.billingAddress?.postalCode,
                "emailAddress" to merchantConfiguration?.buyer?.email,
                "mobilePhone" to merchantConfiguration?.buyer?.phone,
                "alternativePhone" to merchantConfiguration?.buyer?.alternativePhone,
                "storeNumber" to merchantConfiguration?.storeNumber,
                "loyaltyNumber" to merchantConfiguration?.loyaltyID,
                "departmentId" to merchantConfiguration?.departmentId,
                "checkoutAmount" to CommonUtils().fromMoneyToDollars(placementData?.order?.totalPrice?.value),
                "location" to placementData?.locationType,
                "epId" to userTrackingId,
                "epPlacementId" to placementData?.placementId,
                "epSessionId" to sessionId,
                "channel" to merchantConfiguration?.channel,
                "subchannel" to merchantConfiguration?.subchannel,
                "clientVariable1" to merchantConfiguration?.clientVariable1,
                "clientVariable2" to merchantConfiguration?.clientVariable2,
                "clientVariable3" to merchantConfiguration?.clientVariable3,
                "clientVariable4" to merchantConfiguration?.clientVariable4,
                "selectedCardKey" to placementData?.selectedCardKey,
                "defaultSelectedCardKey" to placementData?.defaultSelectedCardKey,
                "overrideKey" to merchantConfiguration?.overrideKey,
                "cardChoiceCode" to merchantConfiguration?.cardChoiceCode,
                "associateId" to merchantConfiguration?.clerkId,
                "splitPayment" to if (merchantConfiguration?.paymentMode == PaymentMode.SPLIT) true else null
            )
        )
    }

    /**
     * Generates path and query string for unified prequalification.
     * Used for standard prequalification flow (not checkout).
     *
     * @param initialData Initial unified prequalification data
     * @param clientKey Client key for the request
     * @return UnifiedPrequalPathResult containing path, query string, and parameters
     */
    fun pathForUnifiedPrequal(
        initialData: MutableMap<String, Any?>,
        clientKey: String,
    ): UnifiedPrequalPathResult {
        val queryParams = mutableMapOf<String, Any?>(
            "embedded" to true,
            "clientKey" to clientKey
        )

        // Merge initial data
        queryParams.putAll(initialData)

        return UnifiedPrequalPathResult(
            path = "/unified/offer-intro",
            queryString = queryParams.toQueryString(),
            queryParams = queryParams
        )
    }

    /**
     * Maps buyer billing address to UPQ address.
     *
     * @param buyer Buyer object containing billing address
     * @return UPQAddressRequest with mapped fields, or null if address is not available
     */
    fun mapUnifiedPlacementContextToUPQAddressRequest(buyer: BreadPartnersBuyer?): UPQAddressRequest? {
        if (buyer?.shippingAddress == null) return null
        return UPQAddressRequest(
            address1 = buyer.shippingAddress?.address1,
            address2 = buyer.shippingAddress?.address2,
            city = buyer.shippingAddress?.locality,
            state = buyer.shippingAddress?.region,
            zip = buyer.shippingAddress?.postalCode
        )
    }


    /**
     * Maps unified placement order to order.
     *
     * @param order Order object from placement config
     * @return Order with mapped fields, or null if order is null
     */
    fun mapUnifiedPlacementOrderToOrder(order: Order?): MutableMap<String, Any?> {
        return CommonUtils().assignDefined(
            mutableMapOf<String, Any?>(),
            mapOf(
                "bnplEligible" to order?.bnplEligible,
                "items" to order?.items?.map { item ->
                    CommonUtils().assignDefined(
                        mutableMapOf<String, Any?>(),
                        mapOf(
                            "name" to item.name,
                            "category" to item.category,
                            "quantity" to item.quantity,
                            "unitPriceValue" to CommonUtils().fromMoneyToDollars(item.unitPrice?.value),
                            "unitTaxValue" to CommonUtils().fromMoneyToDollars(item.unitTax?.value),
                            "sku" to item.sku,
                            // itemUrl: not captured
                            // imageUrl: not captured
                            // description: not captured
                            "shippingCostValue" to CommonUtils().fromMoneyToDollars(item.shippingCost?.value),
                            "fulfillmentType" to item.fulfillmentType
                        )
                    )
                },
                "subTotalValue" to CommonUtils().fromMoneyToDollars(order?.subTotal?.value),
                "totalDiscountsValue" to CommonUtils().fromMoneyToDollars(order?.totalDiscounts?.value),
                "totalPriceValue" to CommonUtils().fromMoneyToDollars(order?.totalPrice?.value),
                "totalShippingValue" to CommonUtils().fromMoneyToDollars(order?.totalShipping?.value),
                "totalTaxValue" to CommonUtils().fromMoneyToDollars(order?.totalTax?.value),
                // discountCode: not captured
                // shippingProvider: not captured
                // shippingDescription: not captured
                // shippingTrackingNumber: not captured
                // shippingTrackingUrl: not captured
                "fulfillmentType" to order?.fulfillmentType,
                "pickupInformation" to if (order?.pickupInformation != null) {
                    CommonUtils().assignDefined(
                        mutableMapOf<String, Any?>(),
                        mapOf(
                            "name" to CommonUtils().assignDefined(
                                mutableMapOf<String, Any?>(),
                                mapOf(
                                    "firstName" to order.pickupInformation?.name?.givenName,
                                    "lastName" to order.pickupInformation?.name?.familyName,
                                    "additionalName" to order.pickupInformation?.name?.additionalName
                                )
                            ),
                            "address" to CommonUtils().assignDefined(
                                mutableMapOf<String, Any?>(),
                                mapOf(
                                    "address1" to order.pickupInformation?.address?.address1,
                                    "address2" to order.pickupInformation?.address?.address2,
                                    "city" to order.pickupInformation?.address?.locality,
                                    "state" to order.pickupInformation?.address?.region,
                                    "zip" to order.pickupInformation?.address?.postalCode
                                )
                            ),
                            "mobilePhone" to order.pickupInformation?.phone,
                            "emailAddress" to order.pickupInformation?.email
                        )
                    )
                } else {
                    null
                }
            )
        )
    }

    /**
     * Generates path and query string for unified prequalification checkout.
     * Used for checkout flow with order information.
     *
     * @param initialData Initial unified prequalification checkout data
     * @param clientKey Client key for the request
     * @return UnifiedPrequalPathResult containing path, query string, and parameters
     */
    fun pathForUnifiedPrequalCheckout(
        initialData: MutableMap<String, Any?>,
        clientKey: String,
    ): UnifiedPrequalPathResult {
        val queryParams = mutableMapOf<String, Any?>(
            "embedded" to true,
            "clientKey" to clientKey
        )
        // Merge initial data
        queryParams.putAll(initialData)

        // Create final params with stringified order and shippingAddress
        val finalParams = queryParams.toMutableMap()

        // Stringify order object if present
        queryParams["order"]?.let { order ->
            finalParams["order"] = Gson().toJson(order)
        }

        // Stringify shippingAddress object if present
        queryParams["shippingAddress"]?.let { shippingAddress ->
            finalParams["shippingAddress"] = Gson().toJson(shippingAddress)
        }


        return UnifiedPrequalPathResult(
            path = "/unified/checkout",
            queryString = finalParams.toQueryString(),
            queryParams = queryParams
        )
    }

    /**
     * Checks BNPL eligibility based on item categories.
     * Sets bnplEligible to false if any item has an ineligible category.
     *
     * @param orderMap Map representing the order object to check
     */
    fun checkBnplEligibility(orderMap: MutableMap<String, Any?>) {
        if (orderMap.isEmpty()) return

        @Suppress("UNCHECKED_CAST")
        val items = orderMap["items"] as? List<MutableMap<String, Any?>> ?: return

        for (item in items) {
            val itemCategory = (item["category"] as? String)?.lowercase()
            if (itemCategory in INELIGIBLE_ITEM_CATEGORIES) {
                orderMap["bnplEligible"] = false
                return
            }
        }
    }


}


