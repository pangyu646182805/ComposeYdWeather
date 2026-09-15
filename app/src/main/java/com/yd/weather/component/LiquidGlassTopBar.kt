package com.yd.weather.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.yd.weather.R

/** 顶栏内容区高度，与 [com.yd.weather.component.CenterTopAppBar] 保持一致 */
val LiquidGlassTopBarContentHeight = 48.dp

/**
 * 模糊向下收尾的过渡段高度。
 *
 * 它**不计入**内容留白：这一段压在内容顶部的空白内边距上，
 * 算进 contentPadding 的话内容会整体下移一个渐隐段。
 */
val LiquidGlassFadeHeight = 12.dp

/** 圆形玻璃按钮的直径 */
private val GlassButtonSize = 44.dp

/**
 * 渐进模糊的一层。
 *
 * @param radius 这一层的模糊半径
 * @param solidTo 从顶部到这个比例为止保持满不透明
 * @param fadeTo 从 [solidTo] 渐隐到这个比例时完全透明
 */
private data class BlurBand(val radius: Dp, val solidTo: Float, val fadeTo: Float)

/**
 * 从清晰到模糊排列，**顺序即绘制顺序**：后画的盖在先画的上面。
 *
 * 于是从上往下看到的是 32dp → 20dp → 10dp → 4dp → 无 的连续梯度，
 * 而不是"一整块统一模糊的图半透明压在清晰内容上"——后者会出现重影，
 * 也就是卡片靠近顶栏时边上那圈发白的光晕。
 */
private val ProgressiveBands = listOf(
    BlurBand(radius = 4.dp, solidTo = 0.70f, fadeTo = 1.00f),
    BlurBand(radius = 10.dp, solidTo = 0.50f, fadeTo = 0.78f),
    BlurBand(radius = 20.dp, solidTo = 0.30f, fadeTo = 0.58f),
    BlurBand(radius = 32.dp, solidTo = 0.12f, fadeTo = 0.38f),
)

/**
 * 顶栏内容留白（状态栏 + 内容区），与改造前 topBar 所占的高度一致。
 *
 * 列表用它作为 contentPadding 的顶部值，内容位置和原来分毫不差，
 * 滚动时又能穿到顶栏背后去。
 *
 * 玻璃层要比它高出一个 [LiquidGlassFadeHeight] 用于渐隐，
 * 那一段正好落在内容自身的顶部内边距上，压不到文字。
 */
@Composable
fun liquidGlassTopBarHeight(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            LiquidGlassTopBarContentHeight

/**
 * 液态玻璃顶栏。
 *
 * 和 Material3 的 TopAppBar 不同，它**不占布局空间**，而是浮在内容之上，
 * 靠 [backdrop] 实时取用背后滚动的内容做模糊与折射。
 *
 * 调用方需要做两件事，缺一不可：
 * 1. 给背景内容加上 `Modifier.layerBackdrop(backdrop)`，声明"我是被取用的那一层"；
 * 2. 给背景内容留出 [liquidGlassTopBarHeight] 的顶部 contentPadding。
 *
 * ⚠️ 背景内容不能包含本组件，否则会自己模糊自己，每帧叠加直到糊成一片白。
 *
 * 模糊与折射依赖 RenderEffect(API 31) 和 RuntimeShader(API 33)，
 * 低版本上 backdrop 库内部会自行降级，此处不额外处理。
 *
 * @param backdrop 背景来源，由 `rememberLayerBackdrop()` 创建
 * @param title 居中标题文案
 * @param titleAlpha 居中标题透明度，配合大标题滚动淡入
 * @param navigationIcon 左侧按钮内容，外层已套好玻璃圆底
 * @param actionIcon 右侧按钮内容，外层已套好玻璃圆底
 * @param actions 右侧自定义内容，原样摆放（文字按钮等不适合套圆底的场景）；
 *                与 [actionIcon] 二选一
 */
@Composable
fun LiquidGlassTopBar(
    backdrop: Backdrop,
    title: String,
    modifier: Modifier = Modifier,
    titleAlpha: Float = 1f,
    navigationIcon: @Composable (() -> Unit)? = null,
    actionIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val barHeight = liquidGlassTopBarHeight() + LiquidGlassFadeHeight

    Box(modifier.fillMaxWidth()) {
        LiquidGlassScrim(backdrop = backdrop, height = barHeight)

        // 内容层：按钮和标题浮在最上面，自身保持清晰
        Box(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(LiquidGlassTopBarContentHeight)
                .padding(horizontal = 16.dp),  // 与列表卡片、大标题的 16dp 边距对齐
        ) {
            if (navigationIcon != null) {
                GlassIconButton(
                    backdrop = backdrop,
                    modifier = Modifier.align(Alignment.CenterStart),
                    content = navigationIcon,
                )
            }

            AppText(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.black).copy(alpha = titleAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (actionIcon != null) {
                GlassIconButton(
                    backdrop = backdrop,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    content = actionIcon,
                )
            } else if (actions != null) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) { actions() }
            }
        }
    }
}

/**
 * 液态玻璃底衬：渐进模糊 + 材质薄纱，可用在顶栏，也可以翻过来用在底栏。
 *
 * @param height 这块玻璃的高度
 * @param fromTop true 表示实心的一端朝上（顶栏），false 表示朝下（底栏）
 */
@Composable
fun LiquidGlassScrim(
    backdrop: Backdrop,
    height: Dp,
    modifier: Modifier = Modifier,
    fromTop: Boolean = true,
) {
    val scrimColor = MaterialTheme.colorScheme.background

    Box(modifier.fillMaxWidth()) {
        // 一、渐进模糊：多层叠加拼出梯度
        ProgressiveBands.forEach { band ->
            BlurBandLayer(backdrop = backdrop, band = band, barHeight = height, fromTop = fromTop)
        }

        // 二、材质薄纱。
        //
        // 这层是"看起来自然"的关键，不是可有可无的修饰。
        // 只有模糊的话，玻璃会忠实显出背后卡片的颜色——页面底色是白的、卡片是橙的，
        // 混出来的灰蓝块跟谁都不搭，那块玻璃就特别扎眼。
        // 铺一层页面底色的薄纱把模糊内容压下去，玻璃才会和页面融为一体，
        // 卡片只是淡淡透出来。iOS 的 ultraThinMaterial 是同一个路子。
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .drawWithCache {
                    // 实心那端拉满不透，往另一端平滑地淡出。
                    // 中间这两档不能省：只给首尾两个色标的话，
                    // 整段会是一条直线，实心端不够实、淡出端又掉得太急。
                    val scrimStops = listOf(
                        0f to scrimColor,
                        0.45f to scrimColor,
                        0.70f to scrimColor.copy(alpha = 0.82f),
                        0.88f to scrimColor.copy(alpha = 0.40f),
                        1f to Color.Transparent,
                    )
                    val brush = Brush.verticalGradient(*orient(scrimStops, fromTop))
                    onDrawBehind { drawRect(brush = brush) }
                }
        )
    }
}

/**
 * 把一组"从实心端起算"的色标摆正。
 *
 * [Brush.verticalGradient] 要求位置递增，所以翻转时不能只把位置取反，
 * 还得把整个列表倒过来。
 */
private fun orient(
    stops: List<Pair<Float, Color>>,
    fromTop: Boolean,
): Array<Pair<Float, Color>> =
    if (fromTop) stops.toTypedArray()
    else stops.map { (pos, color) -> (1f - pos) to color }.reversed().toTypedArray()

/**
 * 渐进模糊的单层。
 *
 * 分父子两个节点是有原因的：DstIn 挖 alpha 必须在离屏缓冲里做，
 * 而把 Offscreen 和 drawBackdrop 串在同一条 modifier 链上时，
 * 外层的离屏合成会把 drawBackdrop 内部的 renderEffect 顶掉（实测模糊完全不生效）。
 * 拆开后，子节点专心出模糊，父节点只管把结果捞进离屏层再挖遮罩。
 */
@Composable
private fun BlurBandLayer(
    backdrop: Backdrop,
    band: BlurBand,
    barHeight: Dp,
    fromTop: Boolean,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                // 色标按比例定位并覆盖整块区域。
                // Brush.verticalGradient 的渐变区间是按绘制区域算的，
                // 只给局部 drawRect 传 brush 会映射不上，等于没挖 alpha。
                val stops = buildList {
                    add(0f to Color.Black)
                    add(band.solidTo to Color.Black)
                    add(band.fadeTo to Color.Transparent)
                    if (band.fadeTo < 1f) add(1f to Color.Transparent)
                }
                val maskBrush = Brush.verticalGradient(*orient(stops, fromTop))
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = maskBrush, blendMode = BlendMode.DstIn)
                }
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RectangleShape },
                    effects = { blur(band.radius.toPx()) },
                )
        )
    }
}

/**
 * 圆形液态玻璃按钮底座。
 *
 * `lens()` 是折射，也是这个效果区别于普通毛玻璃的地方——按钮边缘会把背后的内容掰弯，
 * 这正是 iOS 26 液态玻璃的观感来源。`blur` 刻意给得很小，
 * 因为按钮底下已经是模糊过的顶栏，再叠大半径只会糊成一坨。
 */
@Composable
fun GlassIconButton(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    size: Dp = GlassButtonSize,
    // 跟随主题底色：浅色模式偏白、深色模式偏暗，
    // 写死白色的话深色模式下会变成两个刺眼的白饼。
    tint: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .size(size)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(2f.dp.toPx())
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                },
            )
            // 纯折射的按钮在浅色背景上几乎看不见，补一层淡淡的底色把轮廓撑出来
            .background(tint, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
