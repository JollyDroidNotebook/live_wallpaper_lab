package ru.jollydroid.livewallpaperlab

import android.graphics.Canvas
import android.graphics.Color

class CoolPainter {
    fun draw(c: Canvas) {
        c.save()

        c.drawColor(Color.CYAN)

        c.restore()
    }
}