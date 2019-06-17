package ru.jollydroid.livewallpaperlab

import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class FooBarWallpaperService : WallpaperService() {
    val engineStorage: MutableSet<FooBarEngine> = mutableSetOf()

    override fun onCreateEngine() = FooBarEngine()

    inner class FooBarEngine : Engine() {
        val redrawHandler = RedrawHandler()
        val painter = CoolPainter()

        // my interface
        fun requestRedraw() {
            // ...
        }

        // inherited interface

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            engineStorage.add(this)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            redrawHandler.planRedraw()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            redrawHandler.planRedraw()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            redrawHandler.planRedraw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            redrawHandler.omitRedraw()
        }

        override fun onDestroy() {
            super.onDestroy()
            redrawHandler.omitRedraw()
            engineStorage.remove(this)
        }


        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            redrawHandler.omitRedraw()
            drawSynchronously(holder) // do it immediately, don't plan
        }

        // actions

        // surfaceHolder is call to the getSurfaceHolder() method
        fun drawSynchronously() = drawSynchronously(surfaceHolder)

        fun drawSynchronously(holder: SurfaceHolder) {
            if (!isVisible) return

            var c: Canvas? = null
            try {
                c = holder.lockCanvas()
                c?.let {
                    painter.draw(it)

                }
            } finally {
                c?.let {
                    holder.unlockCanvasAndPost(it)
                }
            }
        }

        // handler

        inner class RedrawHandler : Handler(Looper.getMainLooper()) {
            private val redraw = 1

            fun omitRedraw() {
                removeMessages(redraw)
            }

            fun planRedraw() {
                omitRedraw()
                sendEmptyMessage(redraw)
            }

            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    redraw -> drawSynchronously()

                    else -> super.handleMessage(msg)
                }
            }
        }
    }
}