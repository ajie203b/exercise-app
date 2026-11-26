package com.exercise.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * 配色说明
 *
 * 主色取冷调蓝，用于标题、选中态与链接；辅色为低饱和绿，用于"完成/收藏"之外的正向信息；
 * 第三色为珊瑚橙，专门用于"目标""收藏"这类需要一点点温度的高亮，
 * 避免此前把 error 红当成收藏色造成的语义混淆。
 *
 * 浅色以中性冷灰打底 + 纯白卡片，深色以近黑蓝灰打底，两套都显式给出
 * container / outline 系列，不再依赖 Material3 的紫色基线默认值。
 */

internal val LightColors = lightColorScheme(
    primary = Color(0xFF1665D8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE8FF),
    onPrimaryContainer = Color(0xFF062347),

    secondary = Color(0xFF2C8C5A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6F0DF),
    onSecondaryContainer = Color(0xFF062B18),

    tertiary = Color(0xFFC85A31),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCCF),
    onTertiaryContainer = Color(0xFF3A1206),

    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF14181C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14181C),
    surfaceVariant = Color(0xFFE9EDF1),
    onSurfaceVariant = Color(0xFF4C555E),
    outline = Color(0xFF737C86),
    outlineVariant = Color(0xFFD5DBE1),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF0B2E5C),
    primaryContainer = Color(0xFF1B4A8A),
    onPrimaryContainer = Color(0xFFDCE8FF),

    secondary = Color(0xFF86D5A4),
    onSecondary = Color(0xFF07351D),
    secondaryContainer = Color(0xFF215039),
    onSecondaryContainer = Color(0xFFD6F0DF),

    tertiary = Color(0xFFFFB59C),
    onTertiary = Color(0xFF4A1A0B),
    tertiaryContainer = Color(0xFF6B2E1B),
    onTertiaryContainer = Color(0xFFFFDCCF),

    background = Color(0xFF101417),
    onBackground = Color(0xFFE4E8EC),
    surface = Color(0xFF171C20),
    onSurface = Color(0xFFE4E8EC),
    surfaceVariant = Color(0xFF293036),
    onSurfaceVariant = Color(0xFFB9C2CB),
    outline = Color(0xFF8A939C),
    outlineVariant = Color(0xFF394147),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

/** 动作演示图统一衬底：GIF 本身是 180x180 的纯白底，用同色底板才不会露出方块边 */
internal val PhotoPlate = Color(0xFFFFFFFF)
