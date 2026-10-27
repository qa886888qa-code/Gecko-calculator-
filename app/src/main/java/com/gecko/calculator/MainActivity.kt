package com.gecko.calculator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.android.gms.ads.*

class MainActivity : Activity() {
    private lateinit var display: TextView
    private var current = "0"
    private var stored: Double? = null
    private var op: String? = null
    private var fresh = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 28, 18, 8)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }
        val title = TextView(this).apply {
            text = "GECKO  CALCULATOR"
            textSize = 22f; setTextColor(Color.rgb(110, 231, 183))
            gravity = Gravity.CENTER; setPadding(0, 10, 0, 20)
        }
        root.addView(title)
        display = TextView(this).apply {
            text = "0"; textSize = 42f; gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            setTextColor(Color.WHITE); setPadding(12, 18, 12, 18)
            setBackgroundColor(Color.rgb(30, 41, 59))
        }
        root.addView(display, LinearLayout.LayoutParams(-1, 0, 1.2f))
        val rows = listOf(
            listOf("AC", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )
        rows.forEach { row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { label ->
                val b = Button(this).apply {
                    text = label; textSize = 21f
                    setTextColor(Color.WHITE)
                    setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        if (label in listOf("÷","×","−","+","=")) Color.rgb(5,150,105) else Color.rgb(51,65,85)))
                    setOnClickListener { press(label) }
                }
                line.addView(b, LinearLayout.LayoutParams(0, 0, 1f).apply {
                    height = (resources.displayMetrics.density * 62).toInt()
                    setMargins(4, 4, 4, 4)
                })
            }
            root.addView(line)
        }
        val banner = AdView(this).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = "ca-app-pub-9371973914123866/8812481769"
        }
        root.addView(banner, LinearLayout.LayoutParams(-1, -2))
        banner.loadAd(AdRequest.Builder().build())
        setContentView(root)
    }

    private fun press(k: String) {
        try {
            when (k) {
                "AC" -> { current = "0"; stored = null; op = null; fresh = true }
                "⌫" -> { current = if (current.length > 1) current.dropLast(1) else "0" }
                "." -> if (fresh) { current = "0."; fresh = false } else if (!current.contains(".")) current += "."
                "±" -> current = if (current.startsWith("-")) current.drop(1) else "-$current"
                "%", "÷", "×", "−", "+" -> { stored = current.toDoubleOrNull() ?: 0.0; op = k; fresh = true }
                "=" -> {
                    val a = stored ?: return
                    val b = current.toDoubleOrNull() ?: 0.0
                    val result = when (op) {
                        "+" -> a + b; "−" -> a - b; "×" -> a * b
                        "÷" -> if (b == 0.0) { display.text = "Cannot divide by zero"; return } else a / b
                        "%" -> a % b
                        else -> b
                    }
                    current = if (result.isFinite()) result.toString().removeSuffix(".0") else "Error"
                    stored = null; op = null; fresh = true
                }
                else -> if (fresh || current == "0" || current == "Error") { current = k; fresh = false } else if (current.length < 16) current += k
            }
            display.text = current
        } catch (_: Exception) { current = "Error"; display.text = current }
    }
}
