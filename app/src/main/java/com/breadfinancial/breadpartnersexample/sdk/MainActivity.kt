//------------------------------------------------------------------------------
//  File:          MainActivity.kt
//  Author(s):     Bread Financial
//  Date:          27 March 2025
//
//  Descriptions:  This file is part of the BreadPartnersSDK for Android,
//  providing UI components and functionalities to integrate Bread Financial
//  services into partner applications.
//
//  © 2025 Bread Financial
//------------------------------------------------------------------------------

@file:Suppress("UNUSED_PARAMETER", "unused")

package com.breadfinancial.breadpartnersexample.sdk

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.breadfinancial.breadpartners.sdk.core.BreadPartnersSDK
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnerEvent
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersAddress
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersBuyer
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersEnvironment
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersFinancingType
import com.breadfinancial.breadpartners.sdk.core.models.BreadPartnersLocationType
import com.breadfinancial.breadpartners.sdk.core.models.CurrencyValue
import com.breadfinancial.breadpartners.sdk.core.models.MerchantConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.Name
import com.breadfinancial.breadpartners.sdk.core.models.Order
import com.breadfinancial.breadpartners.sdk.core.models.PickupInformation
import com.breadfinancial.breadpartners.sdk.core.models.PlacementData
import com.breadfinancial.breadpartners.sdk.core.models.PlacementsConfiguration
import com.breadfinancial.breadpartners.sdk.core.models.PopUpStyling
import com.breadfinancial.breadpartners.sdk.core.models.PopupActionButtonStyle
import com.breadfinancial.breadpartners.sdk.core.models.PopupTextStyle
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnersExtensions.replaceButton
import com.breadfinancial.breadpartners.sdk.utilities.BreadPartnersExtensions.replaceTextView
import com.breadfinancial.breadpartnersexample.sdk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        generatePlacement()
    }

    // Reusable Alert
    private fun showYesNoAlert(context: Context, onResult: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Are you authenticated?")

        builder.setPositiveButton("Yes") { dialog: DialogInterface, _ ->
            onResult(true)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog: DialogInterface, _ ->
            onResult(false)
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(Color.parseColor("#d50132"))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(Color.parseColor("#d50132"))
    }

    // Button Click
    fun preScreenCheck(view: View) {
        val bottomSheet = RTPSView()
        bottomSheet.show(supportFragmentManager, "RTPSView")
    }

    // Button Click
    fun openExperience(view: View) {
        val bottomSheet = OpenExperienceView()
        bottomSheet.show(supportFragmentManager, "OpenExperienceView")
    }

    private fun generatePlacement() {
//         For using TestData file do
//        val placementRequestType =
//            TestData.shared.placementConfigurations["textPlacementRequestType0"] ?: emptyMap()
        val placementRequestType = emptyMap<String, Any>()
        val placementID = placementRequestType["placementID"] as String?
        val price = placementRequestType["price"] as? Int?
        val allowCheckout = placementRequestType["allowCheckout"] as? Boolean ?: false
        val loyaltyId = placementRequestType["loyaltyId"] as? String?
        val brandId = placementRequestType["brandId"] as? String
        val channel = placementRequestType["channel"] as? String?
        val subChannel = placementRequestType["subchannel"] as? String?
        val env = placementRequestType["env"] as? BreadPartnersEnvironment?
        val location = placementRequestType["location"] as? BreadPartnersLocationType?
        val breadPartnersFinancingType =
            placementRequestType["financingType"] as? BreadPartnersFinancingType?

        // If using TestData file do: val style = TestData.shared.styleStruct["red"] ?: emptyMap()
        val style = emptyMap<String, Any>()

        val primaryColor = style["primaryColor"] as? String? ?: "#d50132"
        val lightColor = style["lightColor"] as? String ?: "#b8bdc0"
        val darkColor = style["darkColor"] as? String ?: "#000000"
        val boxColor = style["boxColor"] as? String ?: "#ececec"
        val fontFamily = style["fontFamily"] as? String ?: "poppins_bold"
        val smallTextSize = style["smallTextSize"] as? Int ?: 12
        val mediumTextSize = style["mediumTextSize"] as? Int ?: 15
        val largeTextSize = style["largeTextSize"] as? Int ?: 18
        val xlargeTextSize = style["xlargeTextSize"] as? Int ?: 20

        val customFont = ResourcesCompat.getFont(
            this, this.resources.getIdentifier(fontFamily, "font", this.packageName)
        )

        // region Popup Styling
        /**
         * Prepares a popup styling configuration object for each style element.
         */
        val popUpStyling = PopUpStyling(
            loaderColor = Color.parseColor(primaryColor),
            crossColor = Color.parseColor(primaryColor),
            dividerColor = Color.parseColor(boxColor),
            borderColor = Color.parseColor(boxColor),
            titlePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.BOLD
                ), textColor = Color.parseColor(darkColor), textSize = xlargeTextSize.toFloat()
            ),
            subTitlePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.NORMAL
                ), textColor = Color.parseColor(lightColor), textSize = mediumTextSize.toFloat()
            ),
            headerPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.BOLD
                ), textColor = Color.parseColor(darkColor), textSize = mediumTextSize.toFloat()
            ),
            headerBgColor = Color.parseColor(boxColor),
            headingThreePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.BOLD
                ), textColor = Color.parseColor(primaryColor), textSize = largeTextSize.toFloat()
            ),
            paragraphPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.NORMAL
                ), textColor = Color.parseColor(lightColor), textSize = smallTextSize.toFloat()
            ),
            connectorPopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.BOLD
                ), textColor = Color.parseColor(darkColor), textSize = largeTextSize.toFloat()
            ),
            disclosurePopupTextStyle = PopupTextStyle(
                font = Typeface.create(
                    fontFamily, Typeface.NORMAL
                ), textColor = Color.parseColor(lightColor), textSize = smallTextSize.toFloat()
            ),
            actionButtonStyle = PopupActionButtonStyle(
                font = Typeface.create(
                    fontFamily, Typeface.NORMAL
                ),
                textColor = Color.WHITE,
                backgroundColor = Color.parseColor(primaryColor),
                cornerRadius = 30.0F
            )
        )
        // endregion

        binding.preScreenBtn.typeface = customFont
        binding.preScreenBtn.textSize = largeTextSize.toFloat()
        binding.preScreenBtn.setTextColor(Color.parseColor(primaryColor))

        binding.openExperienceBtn.typeface = customFont
        binding.openExperienceBtn.textSize = largeTextSize.toFloat()
        binding.openExperienceBtn.setTextColor(Color.parseColor(primaryColor))

        BreadPartnersSDK.getInstance().setup(
            environment = env ?: BreadPartnersEnvironment.STAGE,
            enableLog = true,
            integrationKey = brandId ?: "",
            application = application
        )
        /**
         * Configuration for defining placement options in BreadPartners.
         *
         * Modify the Placement ID and total price to test different placements.
         */
        val placementData = PlacementData(
            financingType = breadPartnersFinancingType,
            locationType = location,
            placementId = placementID,
            allowCheckout = allowCheckout,
            order = Order(
                subTotal = CurrencyValue(currency = "USD", value = price?.toDouble()),
                totalDiscounts = CurrencyValue(currency = "USD", value = 0.0),
                totalPrice = CurrencyValue(currency = "USD", value = price?.toDouble()),
                totalShipping = CurrencyValue(currency = "USD", value = 0.0),
                totalTax = CurrencyValue(currency = "USD", value = 0.0),
                discountCode = "string",
                pickupInformation = PickupInformation(
                    name = Name(
                        givenName = "John", familyName = "Doe"
                    ), phone = "+14539842345", address = BreadPartnersAddress(
                        address1 = "156 5th Avenue",
                        locality = "New York",
                        postalCode = "10019",
                        region = "US-NY",
                        country = "US"
                    ), email = "john.doe@gmail.com"
                ),
                fulfillmentType = "type",
                items = emptyList()
            )
        )

        val placementsConfiguration = PlacementsConfiguration(
            placementData = placementData, popUpStyling = popUpStyling
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
            loyaltyID = loyaltyId,
            env = env ?: BreadPartnersEnvironment.STAGE,
            channel = channel,
            subchannel = subChannel
        )

        BreadPartnersSDK.getInstance().registerPlacements(
            merchantConfiguration = merchantConfiguration,
            placementsConfiguration = placementsConfiguration,
            viewContext = this,
            splitTextAndAction = false
        ) { event ->
            when (event) {
                is BreadPartnerEvent.RenderTextViewWithLink -> {

                    /**
                     * Handles rendering of a text view with a clickable link.
                     *
                     * - Modifies the font, text color, and link color for the text view.
                     * - Adds the text view to the main view and sets up its layout constraints.
                     */

                    val textView = binding.textView
                    val spannable = event.spannableText
                    
                    // If you want to correctly style the text view with link functionality,
                    // you can call the actionPlacement function,
                    actionPlacement(spannable, primaryColor, textView, customFont)

                    // If you want to correctly style the text view without link functionality,
                    // you can call the noActionPlacement function,
//                     noActionPlacement(textView)

                    textView.text = spannable
                    textView.movementMethod = LinkMovementMethod.getInstance()
                }


                is BreadPartnerEvent.RenderSeparateTextAndButton -> {

                    /**
                     * Handles rendering of a popup view.
                     *
                     * - Modifies the font, text color for the text view, and the button's title color, font, and background.
                     * - Adds the text view and button to the main view and sets up their layout constraints.
                     */
                    event.textView.apply {
                        setTextColor(Color.BLACK)
                        textSize = 24f
                        typeface = customFont
                    }

                    event.button.apply {
                        setTextColor(Color.WHITE)
                        textSize = 24f
                        typeface = customFont
                        setPadding(50, 0, 50, 0)

                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 55.0f
                            setColor(Color.parseColor(primaryColor))
                        }

                        val pressedDrawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 55.0f
                            setColor(Color.parseColor(primaryColor))
                        }

                        val states = StateListDrawable().apply {
                            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
                            addState(intArrayOf(), drawable)
                        }

                        background = states
                    }

                    binding.actionButton.visibility = View.VISIBLE
                    val parent = binding.textView.parent as ViewGroup
                    parent.replaceTextView(binding.textView, event.textView)
                    parent.replaceButton(binding.actionButton, event.button)

                }

                is BreadPartnerEvent.RenderPopupView -> {

                    /**
                     * Handles rendering of a popup view.
                     *
                     * Example:
                     * Implement some process prior to loading the WebView popup
                     * (e.g., checking if the customer is authenticated).
                     */
                    val view = event.dialogFragment
                    view.show(this.supportFragmentManager, "PopupDialog")
                }

                is BreadPartnerEvent.OnSDKEventLog -> {}

                else -> {
                    Log.i("BreadPartnerSDK::", "Event:$event")
                }
            }
        }
    }

    private fun noActionPlacement(textView: TextView) {
        textView.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun actionPlacement(
        spannable: Spannable,
        primaryColor: String,
        textView: TextView,
        customFont: Typeface?
    ) {
        val clickableSpans =
            spannable.getSpans(0, spannable.length, ClickableSpan::class.java)
        val normalTextEndIndex =
            clickableSpans.firstOrNull()?.let { spannable.getSpanStart(it) } ?: 0

        textView.typeface = customFont

        spannable.apply {
            setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                normalTextEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                AbsoluteSizeSpan(17, true),
                0,
                normalTextEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            setSpan(
                ForegroundColorSpan(Color.parseColor(primaryColor)),
                normalTextEndIndex,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            setSpan(
                AbsoluteSizeSpan(17, true),
                normalTextEndIndex,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}



