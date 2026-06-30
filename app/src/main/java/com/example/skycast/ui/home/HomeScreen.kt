package com.example.skycast.ui.home

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import com.example.skycast.data.model.ForecastDay
import com.example.skycast.data.model.Province
import com.example.skycast.data.repository.CityRepository
import kotlin.math.pow

/**
 * ═══════════════════════════════════════════════════════════
 * HomeScreen — 主页面
 *
 * 四层结构（从底到顶）：
 *   zIndex=0（默认）— 背景渐变 + 正文（PullToRefreshBox → Column）
 *   zIndex=2           — 顶部/底部渐变遮罩
 *   zIndex=3           — 顶栏（触摸优先，alpha 控制渐隐）
 *   zIndex=4           — 信号 Tooltip
 * ═══════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cityRepository: CityRepository,
    modifier: Modifier = Modifier,
) {
    // ═══════════════════════════════════════════════════
    // 状态变量
    // ═══════════════════════════════════════════════════
    val uiState by viewModel.uiState.collectAsState()       // 主 UI 状态（加载/数据/错误/需手动输入）
    var showPicker by remember { mutableStateOf(false) }     // 是否显示城市选择弹窗
    var autoPickerShown by remember { mutableStateOf(false) }// 自动弹出城市选择器是否已触发过（只弹一次）
    var showPreview by remember { mutableStateOf(false) }    // 是否显示组件预览页（长按标题进入）
    var signalTooltip by remember { mutableStateOf(false) }  // 是否显示信号状态提示气泡
    var isRefreshing by remember { mutableStateOf(false) }     // 下拉刷新状态
    var provinces by remember { mutableStateOf<List<Province>>(emptyList()) } // 省份列表（从 assets 异步加载）
    val scrollState = rememberScrollState()                                   // 滚动状态（用于顶栏渐隐）

    // ═══════════════════════════════════════════════════
    // 初始化：异步加载省份数据（约280KB JSON）
    // ═══════════════════════════════════════════════════
    LaunchedEffect(Unit) { provinces = cityRepository.loadProvinces() }

    // ═══════════════════════════════════════════════════
    // 条件渲染：预览页 / 自动弹出城市选择器
    // ═══════════════════════════════════════════════════
    if (showPreview) { PreviewScreen(onDismiss = { showPreview = false }); return }   // 预览模式，不渲染主页
    if (uiState.needManualInput && !showPicker && !autoPickerShown)                  // 需要手动输入且未弹过
        if (provinces.isNotEmpty()) { showPicker = true; autoPickerShown = true }     // 自动弹出城市选择器
    if (!uiState.needManualInput && autoPickerShown) autoPickerShown = false         // 定位成功后重置标记

    // ═══════════════════════════════════════════════════
    // 数据派生：天气数据 / 加载动画 / 离线标记
    // ═══════════════════════════════════════════════════
    val combined = uiState.combinedWeather                                     // 合并后的天气数据（实时+预报）
    val hasData = !uiState.isLoading && combined != null                       // 是否有数据可展示
    val progress by animateFloatAsState(                                       // 渐显动画进度：0→1（600ms）
        targetValue = if (hasData) 1f else 0f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    val isOffline = combined?.isFromCache == true                              // 是否离线缓存数据
    val topBarFade by remember { derivedStateOf { val r = (scrollState.value / 90f).coerceIn(0f, 1f); (1f - r.pow(2.5f)) } }  // 顶栏渐隐：二次曲线先慢后快，滚90px完全透明
    val bottomBarFade by remember { derivedStateOf { if (scrollState.maxValue <= 0f) 1f else ((scrollState.maxValue - scrollState.value) / 80f).coerceIn(0f, 1f) } }

    // ═══════════════════════════════════════════════════
    // 刷新结束：加载完成时自动关闭刷新指示器
    // ═══════════════════════════════════════════════════
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) isRefreshing = false }

    // ═══════════════════════════════════════════════════
    // 背景色计算
    //   - weatherText：天气关键词（控制渐变颜色倾向）
    //   - hour：当前小时（控制昼夜渐变）
    //   - targetGrad：目标渐变色（top顶部色, bottom底部色, tint文字色）
    //   - bgTop/bgBottom：带动画过渡的背景色
    // ═══════════════════════════════════════════════════
    val weatherText = combined?.realtime?.weather1?.ifEmpty { "晴" } ?: "晴"
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val targetGrad = if (hasData) cardGradient(hour, weatherText)              // 根据天气+时间计算渐变
                     else CardGradient(Color.White, Color.White, Color.Black) // 无数据时白色

    val bgTop by animateColorAsState(targetGrad.top, label = "bgTop")           // 顶部颜色 → 带动画
    val bgBottom by animateColorAsState(targetGrad.bottom, label = "bgBottom") // 底部颜色 → 带动画
    val textColor = if (bgTop.luminance() > 0.5f) Color.Black else Color.White // 背景亮度 → 文字颜色

    // ═══════════════════════════════════════════════════
    // 系统状态栏 & 导航栏颜色
    //   - 根据背景亮度切换状态栏图标颜色（isLightBg）
    //   - 设置状态栏和导航栏背景色与页面背景一致
    // ═══════════════════════════════════════════════════
    val isLightBg = bgTop.luminance() > 0.5f
    val view = LocalView.current
    DisposableEffect(bgTop, isLightBg) {
        val w = (view.context as Activity).window
        w.statusBarColor = bgTop.toArgb()
        w.navigationBarColor = bgTop.toArgb()
        WindowCompat.getInsetsController(w, view).apply {
            isAppearanceLightStatusBars = isLightBg          // 亮背景→深色状态栏图标
            isAppearanceLightNavigationBars = isLightBg      // 亮背景→深色导航栏图标
        }
        onDispose { }
    }

    // ═══════════════════════════════════════════════════
    // 系统栏高度（用于避让状态栏/导航栏）
    // ═══════════════════════════════════════════════════
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()          // 状态栏高度
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() // 导航栏高度

    // ═══════════════════════════════════════════════════
    // 根布局：全屏 Box，背景直接画渐变
    // ═══════════════════════════════════════════════════
    Box(
        modifier = modifier.fillMaxSize()
            .drawBehind { drawRect(Brush.verticalGradient(listOf(bgTop, bgBottom))) },       // 整屏背景渐变
    ) {
        // ═════════════════════════════════════════════════════════
        // 【底部渐变遮罩】— zIndex=2，盖在正文上面
        //
        // 作用：让底部导航栏与正文之间有一个渐变过渡，避免底部硬切
        //
        // 可调参数：
        //   .height(120.dp) — 遮罩总高度，改这个数字控制遮罩往上延伸多远
        //   Brush 参数：
        //     0f  to Color.Transparent — 顶部：完全透明，露出正文
        //     0.4f to bgBottom.copy(alpha = 0.8f) — 40%处开始变纯色
        //     1f  to bgBottom          — 底部：纯实色（和背景一样）
        // ═════════════════════════════════════════════════════════
        if (progress > 0.01f) {
            // 【底部渐变遮罩】— zIndex=2，盖在正文上面
            // alpha = progress * bottomBarFade：滚到快到底时遮罩渐隐，卡片底部完全可见
            Box(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).zIndex(2f)
                    .height(120.dp)                                                         // ← 改这个数字调遮罩高度
                    .alpha(progress * bottomBarFade)
                    .background(Brush.verticalGradient(
                        0f to Color.Transparent,          // 顶部：透明
                        0.4f to bgBottom.copy(alpha = 0.8f),           // ← 改这个比例调透明段长度
                        1f to bgBottom                                 // 底部：实色
                    )),
            )

            // 【顶部渐变遮罩】— zIndex=2，和底部遮罩同级
            // 卡片滚到状态栏区域时渐变消失，避免硬切
            // alpha = progress * (1-topBarFade)：顶栏渐隐时遮罩同步渐显，交叉过渡
            //   滚动 0px  → topBarFade=1 → alpha=0  遮罩透明（顶栏可见）
            //   滚动 90px → topBarFade=0 → alpha=1  遮罩显现（顶栏隐去）
            // 渐变多段色标：前50%实色→中段过渡→末段透明，消除明显分割线
            // padding(top = topInset) 让遮罩从状态栏下方开始
            Box(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(2f)
                    .padding(top = topInset)
                    .height(120.dp)                                          // ← 改这个数字调遮罩高度
                    .alpha(progress * (1f - topBarFade))
                    .background(Brush.verticalGradient(
                        0f to bgTop,                           //  0%：纯实色
                        0.5f to bgTop.copy(alpha = 0.3f),
                        1f to Color.Transparent                // 100%：完全透明
                    )),
            )
        }

        // ═════════════════════════════════════════════════════════
        // 【顶栏】— zIndex=3（触摸优先，alpha 控制渐隐）
        //
        // 关键：顶栏 zIndex=3 高于卡片，触摸优先，按钮始终可点。
        // alpha 渐隐到 0 时像是被"推走"消失在背景里。
        //
        // 布局参数：
        //   .padding(top = topInset)  — 紧贴状态栏
        //   .padding(start = 20.dp, end = 12.dp) — 左右边距
        // ═════════════════════════════════════════════════════════
        if (progress > 0.01f) {
            Row(
                modifier = Modifier.fillMaxWidth().zIndex(3f)
                    .graphicsLayer { alpha = progress * topBarFade }
                    .background(bgTop)
                    .padding(start = 20.dp, end = 12.dp, top = topInset + 0.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // —— 标题：SkyCast ☁️（长按进入组件预览页）——
                @OptIn(ExperimentalFoundationApi::class)
                Box(Modifier.clip(RoundedCornerShape(8.dp))
                    .combinedClickable(onClick = {}, onLongClick = { showPreview = true })
                    .padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("SkyCast ☁️", fontSize = (18 + (1 - progress) * 14).sp,
                        fontWeight = FontWeight.Bold, maxLines = 1, color = textColor)
                }
                Spacer(Modifier.width(6.dp))

                // —— 信号指示灯：绿点(在线) / 灰点(离线) ——
                SignalIndicator(isOffline = isOffline, onClick = { signalTooltip = !signalTooltip })
                Spacer(Modifier.width(10.dp))

                // —— 城市选择按钮：显示当前城市名，点击弹出城市选择器 ——
                Row(Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { showPicker = true }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(combined?.displayPlace ?: "选择城市",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("▼", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // ═════════════════════════════════════════════════════════
        // 【正文内容】— zIndex=1，覆盖顶栏，可垂直滚动，下拉刷新
        //
        // 关键：Column top padding 仅留 4dp（裁剪边界紧贴状态栏），
        // 用首行 Spacer(56dp) 把卡片初始位置推到顶栏下方。
        // 向上滚动时 Spacer 滚出视野，卡片自然覆盖顶栏（zIndex=1 > 顶栏 zIndex=0）。
        //
        // 包含：加载态 / 错误提示 / 实时天气卡片(WeatherCard) / 15日预报列表
        //
        // 布局参数：
        //   .padding(top = topInset + 4dp)
        //     — 裁剪边界紧贴状态栏，内容可滚到顶栏区域
        //   .padding(bottom = bottomInset + 24.dp)
        //     — 底部留白：导航栏 + 24dp
        // ═════════════════════════════════════════════════════════
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; viewModel.loadWeatherByLocation() },
            modifier = Modifier.fillMaxSize().zIndex(1f),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp,
                        top = topInset + 4.dp,                               // 裁剪边界紧贴状态栏
                        bottom = bottomInset + 24.dp)                       // 底部留白
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // —— 顶栏占位 Spacer（把卡片推到顶栏下方；滚动时此 Spacer 滚出，卡片覆盖顶栏）——
                Spacer(Modifier.height(56.dp))                               // ← 改这个数字调卡片初始位置

                // —— 加载态：居中显示大标题 ——
                if (uiState.isLoading) {
                    Spacer(Modifier.height(120.dp))
                    @OptIn(ExperimentalFoundationApi::class)
                    Text("SkyCast ☁️", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold, fontSize = 32.sp, color = textColor,
                        modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { showPreview = true }))
                    Spacer(Modifier.height(200.dp))
                    return@Column
                }

                // —— 错误提示 ——
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                }

                // —— 有数据时渲染 ——
                if (combined != null) {
                    // —— 实时天气卡片（旧 API 数据） ——
                    if (combined.realtime != null) {
                        WeatherCard(weather = combined.realtime, displayPlace = combined.displayPlace,
                            realtimePlace = combined.realtimePlace, gradientOverride = targetGrad)
                        Spacer(Modifier.height(20.dp))                         // 卡片间距
                    }

                    // —— 15日预报列表（新 API 数据） ——
                    val forecast = combined.forecast
                    if (!forecast.isNullOrEmpty()) {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                .drawBehind { drawRect(Brush.verticalGradient(
                                    listOf(targetGrad.top, targetGrad.bottom))) }           // 预报卡片背景和实时卡片一致
                                .padding(20.dp)) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text("15日预报", style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = targetGrad.tint)
                                    Spacer(Modifier.height(4.dp))
                                    forecast.forEachIndexed { i, d ->
                                        ForecastRow(d, targetGrad.tint)                     // 每一天的预报行
                                        if (i < forecast.size - 1)
                                            HorizontalDivider(Modifier.padding(vertical = 4.dp),
                                                color = targetGrad.tint.copy(alpha = 0.3f)) // 行间分割线
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))                                               // 底部留白
            }
        }

        // ═════════════════════════════════════════════════════════
        // 【信号状态 Tooltip】— zIndex=4，最高层级
        //
        // 点击信号指示灯后弹出，显示"在线"或"离线缓存"提示
        // ═════════════════════════════════════════════════════════
        if (signalTooltip && progress > 0.01f)
            Box(Modifier.align(Alignment.TopCenter).padding(top = topInset + 52.dp).zIndex(4f)) {
                TooltipBox(text = if (isOffline) "离线数据 — 上次缓存的天气，可能不是最新"
                                   else "在线 — 实时联网获取的最新天气数据")
            }
    }

    // ═════════════════════════════════════════════════════════
    // 城市选择弹窗（ModalBottomSheet，三级联动：省→市→区县）
    // ═════════════════════════════════════════════════════════
    if (showPicker)
        CityPickerDialog(provinces = provinces, onDismiss = { showPicker = false },
            onSelected = { p, c, d -> showPicker = false; viewModel.selectCity(p, c, d) })
}

// ═══════════════════════════════════════════════════════════
// 信号指示灯组件
//   在线 → 绿色圆点 + 浅绿底色
//   离线 → 灰色圆点 + 浅灰底色
// ═══════════════════════════════════════════════════════════
@Composable
private fun SignalIndicator(isOffline: Boolean, onClick: () -> Unit) {
    val dotColor = if (isOffline) Color(0xFF9E9E9E) else Color(0xFF4CAF50)    // 点颜色
    val bg = if (isOffline) Color(0xFFE0E0E0) else Color(0xFFE8F5E9)          // 背景色
    Box(Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(16.dp)) {
            drawCircle(color = dotColor.copy(alpha = 0.3f), radius = size.minDimension / 2)  // 外圈光晕
            drawCircle(color = dotColor, radius = size.minDimension * 0.35f)                 // 内圈实心
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Tooltip 气泡组件
//   深色背景 + 白色文字，圆角矩形
// ═══════════════════════════════════════════════════════════
@Composable
private fun TooltipBox(text: String) {
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.inverseSurface)
        .padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.inverseOnSurface)
    }
}

// ═══════════════════════════════════════════════════════════
// 15日预报 — 单行组件
//   左：星期/日期  中：白天天气/夜间天气  右：最高温/最低温
// ═══════════════════════════════════════════════════════════
@Composable
private fun ForecastRow(day: ForecastDay, tint: Color = Color.White) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // —— 星期 + 日期 ——
        Column(Modifier.width(56.dp)) {
            Text(day.week1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = tint)
            Text(day.week2, style = MaterialTheme.typography.labelSmall, color = tint.copy(alpha = 0.7f))
        }
        Spacer(Modifier.width(8.dp))
        // —— 白天天气 / 夜间天气 ——
        Column(Modifier.weight(1f)) {
            Text("白天 ${day.wea1}", style = MaterialTheme.typography.bodyMedium, color = tint)
            Text("夜间 ${day.wea2}", style = MaterialTheme.typography.bodySmall, color = tint.copy(alpha = 0.7f))
        }
        // —— 最高温 / 最低温 ——
        Column(horizontalAlignment = Alignment.End) {
            Text(day.wendu1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = tint)
            Text(day.wendu2, style = MaterialTheme.typography.bodySmall, color = tint.copy(alpha = 0.7f))
        }
    }
}
