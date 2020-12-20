package com.braincrafttask.image_eraser.model

import android.graphics.Paint
import android.graphics.Path

sealed class DrawingState

data class DrawingPath(val paint: Paint, val path: Path): DrawingState()
object Invert: DrawingState()