package com.courtneythan.aboutme.core.designsystem.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A rectangle whose bottom edge melts into ink drips, the signature shape of the design.
 *
 * [depths] are how far each drip hangs below the flat edge, left to right. The same
 * curve math generates the drips in the Figma file, so the two match.
 */
class DripShape(private val depths: List<Dp>) : Shape {

    /** How much taller than its content a dripping container must be. */
    val dripHeight: Dp = (depths.maxOfOrNull { it.value } ?: 0f).dp + 4.dp

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val depthsPx = with(density) { depths.map { it.toPx() } }
        val base = size.height - with(density) { dripHeight.toPx() }
        val segment = size.width / depthsPx.size
        val lip = with(density) { 6.dp.toPx() }

        path.moveTo(0f, 0f)
        path.lineTo(size.width, 0f)
        path.lineTo(size.width, base)
        var x = size.width
        // Walk right-to-left, hanging one rounded drip per segment.
        depthsPx.asReversed().forEach { depth ->
            val next = x - segment
            val mid = x - segment / 2
            val tip = base + depth
            path.cubicTo(x, base + lip, mid + segment * 0.32f, tip, mid, tip)
            path.cubicTo(mid - segment * 0.32f, tip, next, base + lip, next, base)
            x = next
        }
        path.close()
        return Outline.Generic(path)
    }

    companion object {
        val Tall = DripShape(listOf(32, 18, 56, 26, 38, 16, 50, 22).map { it.dp })
        val Hero = DripShape(listOf(48, 28, 40, 22, 52, 18, 44, 24).map { it.dp })
        val Short = DripShape(listOf(26, 16, 44, 20, 30, 14, 40, 18).map { it.dp })
    }
}
