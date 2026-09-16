                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "wave")
                val phaseOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = (2.0 * Math.PI).toFloat(),
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                    ),
                    label = "phase"
                )
