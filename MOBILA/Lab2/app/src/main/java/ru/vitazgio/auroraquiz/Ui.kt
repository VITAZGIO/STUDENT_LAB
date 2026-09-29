package ru.vitazgio.auroraquiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Палитра «в стиле» светлой атмосферы ОС Аврора (аналог Theme.* из Silica)
object AuroraTheme {
    val primary = Color(0xFF141414)
    val secondary = Color(0xFF7C838C)
    val highlight = Color(0xFF1B86C9)
    val secondaryHighlight = Color(0xFF4AA3DB)
    val right = Color(0xFF4CD964)
    val wrong = Color(0xFFFF5A5A)
    val buttonBg = Color(0x47505A64)
    val frame = Color(0xFF8A9099)

    val horizontalPageMargin = 18.dp
    val paddingSmall = 4.dp
    val paddingMedium = 10.dp
    val paddingLarge = 18.dp

    // Фон, похожий на «атмосферу» Авроры: розовато-белый сверху, голубой снизу
    val background = Brush.verticalGradient(
        listOf(
            Color(0xFFF7E8EC),
            Color(0xFFF6F1F6),
            Color(0xFFE6F2FA),
            Color(0xFFB9E2F8),
        )
    )
}

/** Аналог PageHeader: заголовок справа, под ним описание. */
@Composable
fun PageHeader(title: String, description: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = AuroraTheme.horizontalPageMargin,
                end = AuroraTheme.horizontalPageMargin,
                top = 22.dp,
                bottom = 6.dp
            ),
        horizontalAlignment = Alignment.End
    ) {
        Text(title, color = AuroraTheme.highlight, fontSize = 23.sp, textAlign = TextAlign.End)
        if (description != null) {
            Text(description, color = AuroraTheme.secondaryHighlight, fontSize = 15.sp)
        }
    }
}

/** Аналог SectionHeader: заголовок раздела, прижат вправо. */
@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuroraTheme.horizontalPageMargin, vertical = 6.dp),
        color = AuroraTheme.highlight,
        fontSize = 16.sp,
        textAlign = TextAlign.End
    )
}

/** Аналог Separator с выравниванием по центру: линия, тающая к краям. */
@Composable
fun Separator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        AuroraTheme.primary.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                )
            )
    )
}

/** Аналог Button из Silica: полупрозрачная скруглённая плашка, подсветка при нажатии. */
@Composable
fun AuroraButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = AuroraTheme.primary,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val bg = when {
        pressed -> AuroraTheme.highlight.copy(alpha = 0.30f)
        !enabled -> AuroraTheme.buttonBg.copy(alpha = 0.14f)
        else -> AuroraTheme.buttonBg
    }
    Box(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) textColor else AuroraTheme.primary.copy(alpha = 0.35f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Рамка вокруг индикатора прогресса. */
fun Modifier.progressFrame(): Modifier = this
    .border(2.dp, AuroraTheme.frame, RoundedCornerShape(7.dp))
    .padding(AuroraTheme.paddingSmall)
