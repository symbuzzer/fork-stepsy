package com.nvllz.stepsy.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.nvllz.stepsy.R
import com.nvllz.stepsy.util.AppPreferences

class WidgetPlainProvider : AppWidgetProvider() {

    companion object {
        fun updateWidget(context: Context, appWidgetId: Int, steps: Int) {

            val prefs = context.getSharedPreferences("widget_prefs_$appWidgetId", Context.MODE_MULTI_PROCESS)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_plain)

            // Update text content
            val stepsStr = context.resources.getQuantityString(
                R.plurals.steps_text,
                steps,
                steps
            )
            remoteViews.setTextViewText(R.id.widget_plain_steps, stepsStr)

            // Load preferences
            val useDynamicColors = prefs.getBoolean("use_dynamic_colors", android.os.Build.VERSION.SDK_INT >= 31)
            val opacity = prefs.getInt("opacity", 100)
            val textScale = prefs.getInt("text_scale", 100)
            val scaleFactor = textScale / 100f

            // Resolve colors
            val primaryColor = ContextCompat.getColor(
                context,
                if (useDynamicColors && android.os.Build.VERSION.SDK_INT >= 31)
                    R.color.widgetPrimary else R.color.widgetPrimary_default
            )
            val bgColor = ContextCompat.getColor(
                context,
                if (useDynamicColors && android.os.Build.VERSION.SDK_INT >= 31)
                    R.color.widgetBackground else R.color.widgetBackground_default
            )
            val alphaBgColor = ColorUtils.setAlphaComponent(bgColor, (255 * (opacity / 100f)).toInt())

            // Apply styles
            remoteViews.setInt(R.id.widget_plain_container, "setBackgroundColor", alphaBgColor)
            remoteViews.setTextColor(R.id.widget_plain_steps, primaryColor)

            remoteViews.setTextViewTextSize(
                R.id.widget_plain_steps,
                TypedValue.COMPLEX_UNIT_SP,
                22f * scaleFactor
            )

            // Set widget click behavior
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_plain_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val steps = AppPreferences.steps

        appWidgetIds.forEach { id ->
            updateWidget(context, id, steps)
        }
    }
}
