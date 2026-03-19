package com.yd.weather.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yd.weather.R
import com.yd.weather.component.AppText
import com.yd.weather.component.alphaClick
import com.yd.weather.res.CommonIcon
import com.yd.weather.utils.SetStatusBarStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQualityQueryDialog(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val animateDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.bg_color),
        dragHandle = null
    ) {
        SetStatusBarStyle(isLight = false)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Title bar with close button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                AppText(
                    modifier = Modifier.align(Alignment.Center),
                    text = "中国国家环境保护部\n空气质量指数及相关信息",
                    fontSize = 20.sp,
                    color = colorResource(R.color.black),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                CommonIcon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .alphaClick(onClick = animateDismiss)
                        .padding(16.dp),
                    resId = R.mipmap.ic_close_icon1,
                    size = 22.dp,
                    tint = colorResource(R.color.black)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content with gradient bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 20.dp)
            ) {
                // Gradient bar
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorResource(R.color.color_00e301),
                                    colorResource(R.color.color_fdfd01),
                                    colorResource(R.color.color_fd7e01),
                                    colorResource(R.color.color_f70001),
                                    colorResource(R.color.color_98004c),
                                    colorResource(R.color.color_7d0023)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Descriptions
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AirQualityItem(
                        title = "优：0-50",
                        content = "空气质量令人满意，基本无空气污染，各类人群可正常活动"
                    )
                    AirQualityItem(
                        title = "良：51-100",
                        content = "空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响"
                    )
                    AirQualityItem(
                        title = "轻度污染：101-150",
                        content = "儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼"
                    )
                    AirQualityItem(
                        title = "中度污染：151-200",
                        content = "儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼，一般人群适量减少户外运动"
                    )
                    AirQualityItem(
                        title = "重度污染：201-300",
                        content = "儿童、老年人和心脏病、肺病患者应停留在室内，停止户外运动，一般人群减少户外运动"
                    )
                    AirQualityItem(
                        title = "严重污染：大于300",
                        content = "儿童、老年人和病人应当留在室内，避免体力消耗，一般人群应避免户外活动"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AirQualityItem(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        AppText(
            text = title,
            fontSize = 18.sp,
            color = colorResource(R.color.black),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        AppText(
            text = content,
            fontSize = 15.sp,
            color = colorResource(R.color.text_color_01),
            fontWeight = FontWeight.Medium
        )
    }
}
