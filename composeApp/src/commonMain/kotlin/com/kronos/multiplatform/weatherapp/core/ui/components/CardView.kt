package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.primaryDark
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.primaryLight
import org.jetbrains.compose.resources.painterResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.ic_credit_card_amex
import weather_app.composeapp.generated.resources.ic_credit_card_chip
import weather_app.composeapp.generated.resources.ic_credit_card_default
import weather_app.composeapp.generated.resources.ic_credit_card_discover
import weather_app.composeapp.generated.resources.ic_credit_card_mastercard
import weather_app.composeapp.generated.resources.ic_credit_card_visa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseCardView(
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    elevation: Dp = 4.dp,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (interactionSource != null && onClick != null) {
            val isPressed by interactionSource.collectIsPressedAsState()
            if (isPressed) elevation / 2 else elevation
        } else elevation,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "card_elevation"
    )

    Card(
        modifier = modifier
            .padding(4.dp)
            .then(
                if (onClick != null && interactionSource != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        ),
        border = borderStroke
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveBaseCardView(
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    elevation: Dp = 4.dp,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation / 2 else elevation,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "card_elevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .padding(4.dp)
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple()
                    ) { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        ),
        border = borderStroke
    ) {
        content()
    }
}


data class CreditCardInfo(
    var brand: String = "",
    var last4: String = "",
    var expMonth: String = "",
    var expYear: String = "",
    var holderName: String = "",
)

@Composable
fun CreditCardInfoView(
    creditCardInfo: CreditCardInfo,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onAddCardClick: () -> Unit
) {
    val creditCardColor = if (darkTheme) {
        primaryDark
    } else {
        primaryLight
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = creditCardColor),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Card Header with Chip and Card Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip Image
                    Image(
                        painter = painterResource(Res.drawable.ic_credit_card_chip),
                        contentDescription = "Card Chip",
                        modifier = Modifier.size(40.dp)
                    )
                    // Card Number
                    HeaderText(
                        text = "**** **** **** ${creditCardInfo.last4}",
                        size = ComponentSize.SMALL,
                        textColor = Color.White
                    )
                    // Delete Icon
                    Icon(
                        imageVector = Icons.Default.Delete,
                        modifier = Modifier.clickable { onDeleteClick() },
                        contentDescription = "Delete card",
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.6f), // Reduce width to leave space for the image
                    ) {
                        // Cardholder Name
                        BodyText(
                            text = creditCardInfo.holderName,
                            size = ComponentSize.MEDIUM,
                            textColor = Color.White
                        )

                        // Expiry Date
                        BodyText(
                            text = "${creditCardInfo.expMonth}/${creditCardInfo.expYear}",
                            size = ComponentSize.MEDIUM,
                            textColor = Color.White
                        )
                    }

                    // Brand Logo (Image based on the card brand)
                    if (creditCardInfo.brand.isNotBlank()) {
                        Image(
                            painter = painterResource(
                                when (creditCardInfo.brand.lowercase()) {
                                    "visa" -> Res.drawable.ic_credit_card_visa
                                    "mastercard" -> Res.drawable.ic_credit_card_mastercard
                                    "american express" -> Res.drawable.ic_credit_card_amex
                                    "amex" -> Res.drawable.ic_credit_card_amex
                                    "discover" -> Res.drawable.ic_credit_card_discover
                                    else -> Res.drawable.ic_credit_card_default
                                }
                            ),
                            contentDescription = "Card Brand",
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.CenterVertically) // Align image vertically with the row
                        )
                    }
                }
            }
        }
    }
}
