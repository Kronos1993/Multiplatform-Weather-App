package com.kronos.multiplatform.weatherapp.features.home.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.core.ui.components.BaseCardView
import com.kronos.multiplatform.weatherapp.core.ui.components.BodyText
import com.kronos.multiplatform.weatherapp.core.ui.components.ComponentSize
import com.kronos.multiplatform.weatherapp.core.ui.components.LabelText
import com.kronos.multiplatform.weatherapp.core.ui.components.TitleText
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.AppTheme
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.extendedDark
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.extendedLight
import com.kronos.multiplatform.weatherapp.core.util.IExpectedIntents
import com.kronos.multiplatform.weatherapp.core.util.format
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.about_app_description
import weather_app.composeapp.generated.resources.about_copyright
import weather_app.composeapp.generated.resources.about_feature_1
import weather_app.composeapp.generated.resources.about_feature_2
import weather_app.composeapp.generated.resources.about_feature_3
import weather_app.composeapp.generated.resources.about_feature_4
import weather_app.composeapp.generated.resources.about_features_title
import weather_app.composeapp.generated.resources.about_github
import weather_app.composeapp.generated.resources.about_links_title
import weather_app.composeapp.generated.resources.about_tech_1
import weather_app.composeapp.generated.resources.about_tech_2
import weather_app.composeapp.generated.resources.about_tech_3
import weather_app.composeapp.generated.resources.about_technologies
import weather_app.composeapp.generated.resources.about_version
import weather_app.composeapp.generated.resources.ic_copyright
import weather_app.composeapp.generated.resources.ic_weather_app_icon
import weather_app.composeapp.generated.resources.title_about

@Composable
fun AboutHeaderSection(
    appVersion: String,
    darkTheme: Boolean = false,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier = Modifier,
) {
    val appIcon = vectorResource(Res.drawable.ic_weather_app_icon)

    val cardBackgroundColor = if (darkTheme)
        extendedDark.backgroundCardColor.color
    else
        extendedLight.backgroundCardColor.color

    BaseCardView(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        cardBackgroundColor = cardBackgroundColor,
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = alignment,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = appIcon,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                tint = Color.Unspecified,
            )
            TitleText(
                text = "Weather App",
                size = ComponentSize.MEDIUM,
                textColor = Color.White,
                fontWeight = FontWeight.Medium,
            )
            LabelText(
                text = stringResource(Res.string.about_version).format(appVersion),
                size = ComponentSize.SMALL,
                textColor = Color.White,
            )
        }
    }
}

@Composable
fun AboutInfoSection(
    expectedIntents: IExpectedIntents,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
) {
    val githubUrl = "https://github.com/Kronos1993/Multiplatform-Weather-App"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = alignment,
    ) {

        // ── About ──────────────────────────────────────────────────────────
        AboutSectionCard(title = stringResource(Res.string.title_about), darkTheme) {
            BodyText(
                text = stringResource(Res.string.about_app_description),
                size = ComponentSize.MEDIUM,
                maxLines = Int.MAX_VALUE,
                textAlign = TextAlign.Justify,
                textColor = Color.White,
            )
        }

        // ── Features ───────────────────────────────────────────────────────
        AboutSectionCard(title = stringResource(Res.string.about_features_title), darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AboutFeatureRow(
                    icon = Icons.AutoMirrored.Outlined.List,
                    text = stringResource(Res.string.about_feature_1),
                )
                AboutFeatureRow(
                    icon = Icons.Outlined.BarChart,
                    text = stringResource(Res.string.about_feature_2),
                )
                AboutFeatureRow(
                    icon = Icons.Outlined.Bolt,
                    text = stringResource(Res.string.about_feature_3),
                )
                AboutFeatureRow(
                    icon = Icons.Outlined.Language,
                    text = stringResource(Res.string.about_feature_4),
                )
            }
        }

        // ── Technologies ───────────────────────────────────────────────────
        AboutSectionCard(title = stringResource(Res.string.about_technologies), darkTheme) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(
                    Res.string.about_tech_1,
                    Res.string.about_tech_2,
                    Res.string.about_tech_3,
                ).forEach { res ->
                    AboutTechPill(text = stringResource(res))
                }
            }
        }

        // ── Links ──────────────────────────────────────────────────────────
        AboutSectionCard(title = stringResource(Res.string.about_links_title), darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AboutLinkRow(
                    icon = Icons.Outlined.Code,
                    text = stringResource(Res.string.about_github),
                    onClick = { expectedIntents.openBrowser(githubUrl) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_copyright),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    LabelText(
                        text = stringResource(Res.string.about_copyright),
                        size = ComponentSize.SMALL,
                        textColor = Color.White,
                    )
                }
            }
        }
    }
}

// ── Reusable sub-components ───────────────────────────────────────────────────

@Composable
private fun AboutSectionCard(
    title: String,
    darkTheme: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val cardBackgroundColor = if (darkTheme)
        extendedDark.backgroundCardColor.color
    else
        extendedLight.backgroundCardColor.color

    BaseCardView(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        cardBackgroundColor = cardBackgroundColor,
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                TitleText(
                    text = title,
                    size = ComponentSize.SMALL,
                    fontWeight = FontWeight.Bold,
                    textColor = Color.White,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        BodyText(
            text = text,
            size = ComponentSize.MEDIUM,
            textColor = Color.White,
        )
    }
}

@Composable
private fun AboutTechPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(50.dp),
            )
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        LabelText(
            text = text,
            size = ComponentSize.SMALL,
            textColor = Color.White,
        )
    }
}

@Composable
private fun AboutLinkRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        BodyText(
            text = text,
            size = ComponentSize.MEDIUM,
            textColor = Color.White,
        )
    }
}

@Preview(name = "About Screen — Portrait", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AboutScreenPreview() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AboutHeaderSection(
                    "1.2",
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                )
                AboutInfoSection(
                    expectedIntents = object : IExpectedIntents {
                        override fun openBrowser(url: String) {}
                        override fun makeCall(phone: String) {}
                        override fun sendEmail(email: String) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "About Screen — Landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun AboutScreenLandscapePreview() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                AboutHeaderSection(
                    appVersion = "1.0.0",
                    modifier = Modifier.weight(.5f),
                )
                AboutInfoSection(
                    expectedIntents = object : IExpectedIntents {
                        override fun openBrowser(url: String) {}
                        override fun makeCall(phone: String) {}
                        override fun sendEmail(email: String) {}
                    },
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Preview(name = "About Header", showBackground = true, widthDp = 360)
@Composable
private fun AboutHeaderPreview() {
    AppTheme {
        Surface {
            AboutHeaderSection(
                appVersion = "1.0.0",
                alignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "About Info Section", showBackground = true, widthDp = 360)
@Composable
private fun AboutInfoSectionPreview() {
    AppTheme {
        Surface {
            AboutInfoSection(
                expectedIntents = object : IExpectedIntents {
                    override fun openBrowser(url: String) {}
                    override fun makeCall(phone: String) {}
                    override fun sendEmail(email: String) {}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }
    }
}