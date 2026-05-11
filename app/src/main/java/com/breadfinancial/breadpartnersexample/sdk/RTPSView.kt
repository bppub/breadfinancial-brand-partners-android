//------------------------------------------------------------------------------
//  File:          RTPSView.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

package com.breadfinancial.breadpartnersexample.sdk

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breadfinancial.breadpartners.sdk.core.BreadPartnersSDK
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersAddress
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersBuyer
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersLocationType
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersMockOptions
import com.breadfinancial.breadpartners.sdk.core.models.CurrencyValue
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.Order
import com.breadfinancial.breadpartners.sdk.core.models.PlacementsConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.RTPSData
import com.breadfinancial.breadpartnersexample.sdk.databinding.ViewRtpsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RTPSView : BottomSheetDialogFragment() {

    private lateinit var binding: ViewRtpsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ViewRtpsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height =
                (resources.displayMetrics.heightPixels * 0.9).toInt()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rtpsFlow()
    }


    fun rtpsFlow() {
        val rtpsData = RTPSData(
            locationType = BreadPartnersLocationType.CHECKOUT, order = Order(
                totalPrice = CurrencyValue(
                    currency = "USD", value = 5000
                )
            ), mockResponse = BreadPartnersMockOptions.SUCCESS
        )

        val placementsConfiguration = PlacementsConfiguration(
            rtpsData = rtpsData
        )

        val merchantConfiguration = MerchantConfiguration(
            buyer = BreadPartnersBuyer(
                givenName = "Jack",
                familyName = "Seamus",
                additionalName = "C.",
                birthDate = "1974-08-21",
                email = "johncseamus@gmail.com",
                phone = "+13235323423",
                billingAddress = BreadPartnersAddress(
                    address1 = "323 something lane",
                    address2 = "apt. B",
                    country = "USA",
                    locality = "NYC",
                    region = "NY",
                    postalCode = "11222"
                ),
                shippingAddress = null
            ),
            storeNumber = "8883",
            env = BreadPartnersEnvironment.STAGE
        )

        BreadPartnersSDK.getInstance().silentRTPSRequest(
            merchantConfiguration = merchantConfiguration,
            placementsConfiguration = placementsConfiguration,
            viewContext = requireContext()
        ) { event ->
            when (event) {
                is BreadPartnerEvent.RenderPopupView -> {
                    val view = event.dialogFragment
                    view.show(parentFragmentManager, "PopupDialog")
                    Log.i("BreadPartnerSDK::", "Successfully rendered PopupView.")
                }

                else -> {
                    Log.i("BreadPartnerSDK::", "Event:$event")
                }
            }
        }
    }
}