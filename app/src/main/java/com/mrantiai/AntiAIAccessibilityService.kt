package com.mrantiai.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mrantiai.data.StatsManager

class AntiAIAccessibilityService : AccessibilityService() {

    private var lastSkipTime: Long = 0L
    private val DEBOUNCE_MS = 1400L

    private val targetLabels = listOf(
        "altered or synthetic content",
        "synthetic content",
        "sound or visual effects were used to edit or create",
        "audio or visuals were significantly edited",
        "ai info",
        "made with ai",
        "created with ai",
        "imagined with ai"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return
        val isYouTube = pkg.contains("youtube")
        val isInstagram = pkg.contains("instagram")

        if (!isYouTube && !isInstagram) return

        val now = SystemClock.uptimeMillis()
        if (now - lastSkipTime < DEBOUNCE_MS) return

        val rootNode = rootInActiveWindow ?: return

        try {
            if (scanForAILabels(rootNode)) {
                lastSkipTime = now
                performSwipeUp()
                StatsManager.incrementSkipped(applicationContext, isYouTube)
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun scanForAILabels(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase()
        val desc = node.contentDescription?.toString()?.lowercase()

        for (label in targetLabels) {
            if (text?.contains(label) == true || desc?.contains(label) == true) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = scanForAILabels(child)
            child.recycle()
            if (found) return true
        }

        return false
    }

    private fun performSwipeUp() {
        val metrics: DisplayMetrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val startX = (width / 2).toFloat()
        val startY = (height * 0.78).toFloat()
        val endY = (height * 0.22).toFloat()

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(startX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, 220)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, null, null)
    }

    override fun onInterrupt() {}
}
