package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants for the exact logo palette
val LogoNavy = Color(0xFF0F2E4C)
val LogoGreen = Color(0xFF419D2B)
val LogoSilver = Color(0xFF90A4AE)
val LogoSilverLight = Color(0xFFCFD8DC)

@Composable
fun DairyHubLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 140.dp,
    showText: Boolean = true,
    textColor: Color = LogoNavy
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. The Circular Handcrafted Vector Illustration
        Box(
            modifier = Modifier
                .size(logoSize)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = logoSize.toPx()
                val height = logoSize.toPx()
                val cx = width / 2f
                val cy = height / 2f
                val radius = (width.coerceAtMost(height) / 2f) * 0.95f

                // --- DECORATIVE OUTER ARC ---
                // Left/Top Navy Arc
                drawArc(
                    color = LogoNavy,
                    startAngle = 135f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                // Right Green Arc
                drawArc(
                    color = LogoGreen,
                    startAngle = -45f,
                    sweepAngle = 130f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // --- ROLLING GREEN HILLS (Bottom Right Inside the Arc) ---
                val hillPath = Path().apply {
                    moveTo(cx - radius * 0.2f, cy + radius * 0.8f)
                    quadraticTo(
                        cx + radius * 0.4f, cy + radius * 0.2f,
                        cx + radius * 1.0f, cy + radius * 0.5f
                    )
                    lineTo(cx + radius * 0.8f, cy + radius * 0.8f)
                    quadraticTo(
                        cx + radius * 0.3f, cy + radius * 0.5f,
                        cx - radius * 0.1f, cy + radius * 0.9f
                    )
                    close()
                }
                drawPath(hillPath, color = LogoGreen)

                val hillPath2 = Path().apply {
                    moveTo(cx - radius * 0.5f, cy + radius * 0.85f)
                    quadraticTo(
                        cx + radius * 0.3f, cy + radius * 0.4f,
                        cx + radius * 1.0f, cy + radius * 0.1f
                    )
                    lineTo(cx + radius, cy + radius)
                    lineTo(cx - radius * 0.5f, cy + radius)
                    close()
                }
                drawPath(hillPath2, color = LogoGreen.copy(alpha = 0.15f))

                // --- FARM BARN HOUSE & TREES ---
                // Blue Barn (X door) at bottom right
                val barnX = cx + radius * 0.3f
                val barnY = cy + radius * 0.05f
                val barnW = radius * 0.35f
                val barnH = radius * 0.28f

                // Barn body
                drawRect(
                    color = LogoNavy,
                    topLeft = Offset(barnX, barnY + barnH * 0.3f),
                    size = Size(barnW, barnH * 0.7f)
                )
                // Barn roof
                val roofPath = Path().apply {
                    moveTo(barnX - barnW * 0.05f, barnY + barnH * 0.3f)
                    lineTo(barnX + barnW / 2f, barnY)
                    lineTo(barnX + barnW * 1.05f, barnY + barnH * 0.3f)
                    close()
                }
                drawPath(roofPath, color = LogoNavy)

                // Barn Door with X
                val doorW = barnW * 0.35f
                val doorH = barnH * 0.4f
                val doorX = barnX + (barnW - doorW) / 2f
                val doorY = barnY + barnH - doorH

                drawRect(
                    color = Color.White,
                    topLeft = Offset(doorX, doorY),
                    size = Size(doorW, doorH)
                )
                // X on door
                drawLine(
                    color = LogoNavy,
                    start = Offset(doorX, doorY),
                    end = Offset(doorX + doorW, doorY + doorH),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = LogoNavy,
                    start = Offset(doorX + doorW, doorY),
                    end = Offset(doorX, doorY + doorH),
                    strokeWidth = 1.dp.toPx()
                )

                // Simple Green Tree next to Barn
                val treeX = cx + radius * 0.65f
                val treeY = cy + radius * 0.12f
                val treeR = radius * 0.18f
                // Trunk
                drawRect(
                    color = LogoNavy,
                    topLeft = Offset(treeX - 2.dp.toPx(), treeY),
                    size = Size(4.dp.toPx(), radius * 0.25f)
                )
                // Canopy
                drawCircle(
                    color = LogoGreen,
                    radius = treeR,
                    center = Offset(treeX, treeY - 4.dp.toPx())
                )

                // --- SILVER MILK CAN (CHURN) IN THE CENTER ---
                val canX = cx - radius * 0.05f
                val canY = cy + radius * 0.02f
                val canW = radius * 0.35f
                val canH = radius * 0.55f

                // Base / body gradient simulation
                drawRoundRect(
                    color = LogoSilver,
                    topLeft = Offset(canX, canY + canH * 0.25f),
                    size = Size(canW, canH * 0.75f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRoundRect(
                    color = LogoSilverLight,
                    topLeft = Offset(canX + 3.dp.toPx(), canY + canH * 0.25f),
                    size = Size(canW * 0.4f, canH * 0.75f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                // Can neck / top
                drawRect(
                    color = LogoSilver,
                    topLeft = Offset(canX + canW * 0.15f, canY + canH * 0.1f),
                    size = Size(canW * 0.7f, canH * 0.15f)
                )
                // Can lid
                drawRoundRect(
                    color = LogoNavy,
                    topLeft = Offset(canX + canW * 0.1f, canY),
                    size = Size(canW * 0.8f, canH * 0.12f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                // Churn handle (left)
                val handlePathLeft = Path().apply {
                    moveTo(canX + canW * 0.15f, canY + canH * 0.25f)
                    quadraticTo(
                        canX - canW * 0.15f, canY + canH * 0.35f,
                        canX + canW * 0.15f, canY + canH * 0.45f
                    )
                }
                drawPath(
                    handlePathLeft,
                    color = LogoNavy,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // --- WHITE MILK SPLASH (Flowing from churn to bottom left) ---
                val splashPath = Path().apply {
                    moveTo(canX + canW * 0.3f, cy + radius * 0.55f)
                    quadraticTo(
                        cx - radius * 0.2f, cy + radius * 0.48f,
                        cx - radius * 0.5f, cy + radius * 0.7f
                    )
                    quadraticTo(
                        cx - radius * 0.1f, cy + radius * 0.85f,
                        canX + canW * 0.4f, cy + radius * 0.65f
                    )
                    close()
                }
                drawPath(splashPath, color = Color.White)
                drawPath(splashPath, color = LogoNavy.copy(alpha = 0.1f), style = Stroke(width = 1.dp.toPx()))

                // --- COW HEAD ILLUSTRATION (Top Left and Center) ---
                // Let's build a majestic cow facing forward
                val cowCX = cx - radius * 0.45f
                val cowCY = cy + radius * 0.05f
                val cowW = radius * 0.65f
                val cowH = radius * 0.65f

                // Cow Ears (Background first)
                // Left Ear
                val leftEar = Path().apply {
                    moveTo(cowCX - cowW * 0.2f, cowCY - cowH * 0.15f)
                    quadraticTo(
                        cowCX - cowW * 0.7f, cowCY - cowH * 0.25f,
                        cowCX - cowW * 0.4f, cowCY + cowH * 0.05f
                    )
                    close()
                }
                drawPath(leftEar, color = LogoNavy)

                // Right Ear
                val rightEar = Path().apply {
                    moveTo(cowCX + cowW * 0.2f, cowCY - cowH * 0.15f)
                    quadraticTo(
                        cowCX + cowW * 0.7f, cowCY - cowH * 0.25f,
                        cowCX + cowW * 0.4f, cowCY + cowH * 0.05f
                    )
                    close()
                }
                drawPath(rightEar, color = LogoNavy)

                // Cow Horns
                // Left Horn
                val leftHorn = Path().apply {
                    moveTo(cowCX - cowW * 0.15f, cowCY - cowH * 0.3f)
                    quadraticTo(
                        cowCX - cowW * 0.45f, cowCY - cowH * 0.55f,
                        cowCX - cowW * 0.35f, cowCY - cowH * 0.65f
                    )
                    quadraticTo(
                        cowCX - cowW * 0.2f, cowCY - cowH * 0.5f,
                        cowCX - cowW * 0.08f, cowCY - cowH * 0.35f
                    )
                    close()
                }
                drawPath(leftHorn, color = LogoNavy)

                // Right Horn
                val rightHorn = Path().apply {
                    moveTo(cowCX + cowW * 0.15f, cowCY - cowH * 0.3f)
                    quadraticTo(
                        cowCX + cowW * 0.45f, cowCY - cowH * 0.55f,
                        cowCX + cowW * 0.35f, cowCY - cowH * 0.65f
                    )
                    quadraticTo(
                        cowCX + cowW * 0.2f, cowCY - cowH * 0.5f,
                        cowCX + cowW * 0.08f, cowCY - cowH * 0.35f
                    )
                    close()
                }
                drawPath(rightHorn, color = LogoNavy)

                // Cow Main Face (Base white shape)
                val facePath = Path().apply {
                    moveTo(cowCX - cowW * 0.25f, cowCY - cowH * 0.35f)
                    lineTo(cowCX + cowW * 0.25f, cowCY - cowH * 0.35f)
                    quadraticTo(cowCX + cowW * 0.32f, cowCY, cowCX + cowW * 0.25f, cowCY + cowH * 0.25f)
                    lineTo(cowCX - cowW * 0.25f, cowCY + cowH * 0.25f)
                    quadraticTo(cowCX - cowW * 0.32f, cowCY, cowCX - cowW * 0.25f, cowCY - cowH * 0.35f)
                    close()
                }
                drawPath(facePath, color = Color.White)

                // Cow Black Patches on Face
                val patchLeft = Path().apply {
                    moveTo(cowCX - cowW * 0.25f, cowCY - cowH * 0.35f)
                    lineTo(cowCX, cowCY - cowH * 0.35f)
                    quadraticTo(cowCX - cowW * 0.05f, cowCY - cowH * 0.1f, cowCX - cowW * 0.28f, cowCY)
                    close()
                }
                drawPath(patchLeft, color = LogoNavy)

                val patchRight = Path().apply {
                    moveTo(cowCX + cowW * 0.25f, cowCY)
                    quadraticTo(cowCX + cowW * 0.15f, cowCY + cowH * 0.15f, cowCX + cowW * 0.25f, cowCY + cowH * 0.25f)
                    lineTo(cowCX + cowW * 0.05f, cowCY + cowH * 0.25f)
                    quadraticTo(cowCX + cowW * 0.12f, cowCY + cowH * 0.05f, cowCX + cowW * 0.25f, cowCY)
                    close()
                }
                drawPath(patchRight, color = LogoNavy)

                // Cow Muzzle (Nose area - beautiful silver-pinkish/dark navy outline)
                val muzzlePath = Path().apply {
                    moveTo(cowCX - cowW * 0.25f, cowCY + cowH * 0.22f)
                    lineTo(cowCX + cowW * 0.25f, cowCY + cowH * 0.22f)
                    quadraticTo(
                        cowCX + cowW * 0.22f, cowCY + cowH * 0.45f,
                        cowCX, cowCY + cowH * 0.45f
                    )
                    quadraticTo(
                        cowCX - cowW * 0.22f, cowCY + cowH * 0.45f,
                        cowCX - cowW * 0.25f, cowCY + cowH * 0.22f
                    )
                    close()
                }
                drawPath(muzzlePath, color = LogoNavy)

                // Cow Nostrils
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(cowCX - cowW * 0.08f, cowCY + cowH * 0.32f))
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(cowCX + cowW * 0.08f, cowCY + cowH * 0.32f))

                // Cow Eyes
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(cowCX - cowW * 0.12f, cowCY - cowH * 0.1f))
                drawCircle(color = LogoNavy, radius = 1.5.dp.toPx(), center = Offset(cowCX - cowW * 0.12f, cowCY - cowH * 0.1f))

                drawCircle(color = LogoNavy, radius = 3.dp.toPx(), center = Offset(cowCX + cowW * 0.12f, cowCY - cowH * 0.1f))
                drawCircle(color = Color.White, radius = 1.2.dp.toPx(), center = Offset(cowCX + cowW * 0.14f, cowCY - cowH * 0.12f))

                // Cow neck / body on the left side
                val cowBodyPath = Path().apply {
                    moveTo(cowCX - cowW * 0.3f, cowCY + cowH * 0.25f)
                    quadraticTo(
                        cowCX - cowW * 0.5f, cowCY + cowH * 0.6f,
                        cowCX - cowW * 0.4f, cy + radius * 0.9f
                    )
                    lineTo(cx - radius * 0.1f, cy + radius * 0.9f)
                    quadraticTo(
                        cowCX + cowW * 0.1f, cowCY + cowH * 0.6f,
                        cowCX + cowW * 0.15f, cowCY + cowH * 0.25f
                    )
                    close()
                }
                drawPath(cowBodyPath, color = LogoNavy)

                // White spot on body
                val bodySpot = Path().apply {
                    moveTo(cowCX - cowW * 0.28f, cowCY + cowH * 0.45f)
                    quadraticTo(
                        cowCX - cowW * 0.35f, cowCY + cowH * 0.7f,
                        cowCX - cowW * 0.15f, cy + radius * 0.88f
                    )
                    quadraticTo(
                        cowCX - cowW * 0.12f, cowCY + cowH * 0.65f,
                        cowCX - cowW * 0.28f, cowCY + cowH * 0.45f
                    )
                    close()
                }
                drawPath(bodySpot, color = Color.White)
            }
        }

        // 2. Exact Custom Typography
        if (showText) {
            Spacer(modifier = Modifier.height(10.dp))

            // DAIRY HUB Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // DAIRY - with a styled "A" containing a droplet inside
                Text(
                    text = "D",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
                
                // Stylized A with water drop cutout
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 24.dp)
                        .padding(horizontal = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Triangle A Shape
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, 2.dp.toPx())
                            lineTo(size.width - 1.dp.toPx(), size.height - 2.dp.toPx())
                            lineTo(1.dp.toPx(), size.height - 2.dp.toPx())
                            close()
                        }
                        drawPath(path, color = textColor)
                        
                        // Drop cutout
                        val dropPath = Path().apply {
                            moveTo(size.width / 2f, size.height * 0.45f)
                            quadraticTo(
                                size.width * 0.25f, size.height * 0.72f,
                                size.width / 2f, size.height * 0.82f
                            )
                            quadraticTo(
                                size.width * 0.75f, size.height * 0.72f,
                                size.width / 2f, size.height * 0.45f
                            )
                        }
                        drawPath(dropPath, color = Color.White)
                    }
                }

                Text(
                    text = "IRY",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )

                // HUB - in vibrant green
                Text(
                    text = "HUB",
                    color = LogoGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thin Horizontal Divider Line with Side Arcs
            Row(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(textColor)
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(LogoGreen)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // SMART DAIRY, STRONG FUTURE
            Text(
                text = "SMART DAIRY, STRONG FUTURE",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
