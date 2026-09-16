                    // Background Live Activity Graph (Animated Wave)
                    if (timerState.totalSeconds > 0) {
                        val innerRadius = (diameter / 2) - 16.dp.toPx()
                        val graphWidth = innerRadius * 1.5f
                        val startXOffset = (size.width - graphWidth) / 2
                        val graphHeight = 50.dp.toPx()
                        val baseY = size.height / 2 + 30.dp.toPx()
                        
                        val wavePath = androidx.compose.ui.graphics.Path()
                        val pointsCount = 50
                        
                        val amplitude = (graphHeight / 2) * graphScaleY

                        for (i in 0..pointsCount) {
                            val fraction = i.toFloat() / pointsCount
                            val x = startXOffset + (fraction * graphWidth)
                            
                            val wave1 = kotlin.math.sin((fraction * 4f * Math.PI) + phaseOffset)
                            val wave2 = kotlin.math.sin((fraction * 2f * Math.PI) - (phaseOffset * 1.5f))
                            val combinedWave = (wave1 + wave2) / 2f
                            
                            val y = baseY + (combinedWave * amplitude).toFloat()
                            
                            if (i == 0) {
                                wavePath.moveTo(x, y)
                            } else {
                                wavePath.lineTo(x, y)
                            }
                        }
                        
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(wavePath)
                            lineTo(startXOffset + graphWidth, baseY + graphHeight)
                            lineTo(startXOffset, baseY + graphHeight)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(graphColor.copy(alpha = 0.8f), graphColor.copy(alpha = 0.1f)),
                                startY = baseY - amplitude,
                                endY = baseY + graphHeight
                            )
                        )
                    }
