package com.braincrafttask.image_eraser.model

import android.graphics.Paint
import android.graphics.Path

sealed class EraserState

data class Erase(val paint: Paint, val path: Path): EraserState()
class Invert: EraserState()