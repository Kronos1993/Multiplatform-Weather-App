package com.kronos.multiplatform.weatherapp.features.home.about

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.core.ui.components.BodyText
import com.kronos.multiplatform.weatherapp.core.ui.components.ComponentSize
import com.kronos.multiplatform.weatherapp.core.ui.components.TitleText
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
import weather_app.composeapp.generated.resources.about_tech_1
import weather_app.composeapp.generated.resources.about_tech_2
import weather_app.composeapp.generated.resources.about_tech_3
import weather_app.composeapp.generated.resources.about_tech_4
import weather_app.composeapp.generated.resources.about_technologies
import weather_app.composeapp.generated.resources.about_version
import weather_app.composeapp.generated.resources.compose_multiplatform
import weather_app.composeapp.generated.resources.ic_copyright
import weather_app.composeapp.generated.resources.ic_github
import weather_app.composeapp.generated.resources.ic_weather_app_icon

@Composable
fun AboutHeaderSection(
    isDarkTheme: Boolean,
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
) {
    val appIcon = vectorResource(Res.drawable.ic_weather_app_icon)

    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
    ) {
        Icon(
            appIcon,
            contentDescription = "App Icon",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AboutInfoSection(
    appVersion: String,
    expectedIntents: IExpectedIntents,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val description = stringResource(Res.string.about_app_description)
    val githubUrl = "https://github.com/Kronos1993/Multiplatform-Weather-App"
    val copyright = stringResource(Res.string.about_copyright)
    val featuresTitle = stringResource(Res.string.about_features_title)
    val technologiesTitle = stringResource(Res.string.about_technologies)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = alignment
    ) {
        BodyText(
            text = description,
            modifier = Modifier.padding(horizontal = 8.dp),
            size = ComponentSize.MEDIUM,
            maxLines = Int.MAX_VALUE,
            textColor = Color.White,
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(16.dp))

        TitleText(
            text = featuresTitle,
            size = ComponentSize.SMALL,
            textColor = Color.White,
            fontWeight = FontWeight.Bold
        )

        BodyText(
            text = stringResource(Res.string.about_feature_1),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
        BodyText(
            text = stringResource(Res.string.about_feature_2),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
        BodyText(
            text = stringResource(Res.string.about_feature_3),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
        BodyText(
            text = stringResource(Res.string.about_feature_4),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        Spacer(modifier = Modifier.height(8.dp))

        TitleText(
            text = technologiesTitle,
            size = ComponentSize.SMALL,
            textColor = Color.White,
            fontWeight = FontWeight.Bold
        )

        BodyText(
            text = stringResource(Res.string.about_tech_1),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
        BodyText(
            text = stringResource(Res.string.about_tech_2),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
        BodyText(
            text = stringResource(Res.string.about_tech_3),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
        BodyText(
            text = stringResource(Res.string.about_tech_4),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        Spacer(modifier = Modifier.height(16.dp))

        BodyText(
            text = stringResource(Res.string.about_github),
            vector = vectorResource(Res.drawable.ic_github),
            iconTint = Color.White,
            textColor = Color.White,
            modifier = Modifier.clickable {
                expectedIntents.openBrowser(githubUrl)
            },
            size = ComponentSize.MEDIUM
        )

        BodyText(
            text = stringResource(Res.string.about_version).format(appVersion),
            vector = vectorResource(Res.drawable.compose_multiplatform),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        BodyText(
            vector = vectorResource(Res.drawable.ic_copyright),
            iconTint = Color.White,
            textColor = Color.White,
            text = copyright,
            size = ComponentSize.MEDIUM
        )
    }
}