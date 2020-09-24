package com.yasinkacmaz.playground.ui.widget

import androidx.compose.foundation.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview

class BottomArcShape(private val arcHeight: Float) : Shape {
    override fun createOutline(size: Size, density: Density): Outline {
        val path = Path().apply {
            moveTo(size.width, 0f)
            lineTo(size.width, size.height)
            val arcOffset = arcHeight / 4
            val rect = Rect(
                0f - arcOffset,
                size.height - arcHeight,
                size.width + arcOffset,
                size.height
            )
            arcTo(rect, 0f, 180f, false)
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
@Preview
fun BottomArcShapePreview() {
    Box(
        shape = BottomArcShape(100.dp.value * DensityAmbient.current.density),
        backgroundColor = Color.Magenta,
        modifier = Modifier.size(200.dp, 300.dp)
    )
}
