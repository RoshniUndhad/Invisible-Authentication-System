package com.example.invisibleauthenticationsystem.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.invisibleauthenticationsystem.R
import com.example.invisibleauthenticationsystem.database.DatabaseHelper
import com.google.android.material.button.MaterialButton

class CalculatorActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var dbHelper: DatabaseHelper
    private var currentInput = ""
    
    // The master code to launch the real app (for first time users before registering a PIN)
    private val MASTER_PIN = "80085"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        dbHelper = DatabaseHelper(this)
        tvDisplay = findViewById(R.id.tvDisplay)

        setupButtons()
    }

    private fun setupButtons() {
        // Find all buttons in the layout that are MaterialButtons
        val rootView = findViewById<View>(android.R.id.content)
        val buttons = getAllButtons(rootView)

        for (btn in buttons) {
            btn.setOnClickListener { view ->
                val b = view as MaterialButton
                val buttonText = b.text.toString()

                when (buttonText) {
                    "AC" -> clear()
                    "=" -> calculateResult()
                    "+", "-", "×", "÷" -> addOperator(buttonText)
                    else -> appendNumber(buttonText) // Handles numbers and decimal
                }
            }
        }
    }

    private fun getAllButtons(v: View): List<MaterialButton> {
        val buttons = mutableListOf<MaterialButton>()
        if (v is MaterialButton) {
            buttons.add(v)
        } else if (v is android.view.ViewGroup) {
            for (i in 0 until v.childCount) {
                buttons.addAll(getAllButtons(v.getChildAt(i)))
            }
        }
        return buttons
    }

    private fun clear() {
        currentInput = ""
        updateDisplay("0")
    }

    private fun appendNumber(number: String) {
        if (currentInput == "0" && number != ".") {
            currentInput = number
        } else {
            currentInput += number
        }
        updateDisplay(currentInput)
    }

    private fun addOperator(operator: String) {
        if (currentInput.isNotEmpty() && !currentInput.endsWith(" ")) {
            currentInput += " $operator "
            updateDisplay(currentInput)
        }
    }

    private fun calculateResult() {
        // Check for PIN before evaluating math
        if (currentInput == MASTER_PIN || dbHelper.checkPinExists(currentInput)) {
            launchVault()
            return
        }

        try {
            // Very basic evaluator for the illusion. 
            // In a real stealth app, you'd implement a full math parser.
            val result = evaluateMath(currentInput)
            updateDisplay(result)
            currentInput = result
        } catch (e: Exception) {
            updateDisplay("Error")
            currentInput = ""
        }
    }

    private fun evaluateMath(expression: String): String {
        // This is a placeholder for basic math parsing if you want the calculator to actually work.
        // For the stealth illusion, returning a fake or raw string if it fails is fine.
        val parts = expression.split(" ")
        if (parts.size == 3) {
            val a = parts[0].toDoubleOrNull() ?: 0.0
            val b = parts[2].toDoubleOrNull() ?: 0.0
            
            val res = when(parts[1]) {
                "+" -> a + b
                "-" -> a - b
                "×" -> a * b
                "÷" -> if(b != 0.0) a / b else Double.NaN
                else -> 0.0
            }
            
            // Format to remove .0 if it's a whole number
            return if(res % 1.0 == 0.0) res.toLong().toString() else res.toString()
        }
        return expression
    }

    private fun updateDisplay(text: String) {
        tvDisplay.text = text
    }

    private fun launchVault() {
        // Secret PIN entered! Launch the REAL app (Login Screen)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        // Add a smooth fade to make the transition feel native
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        
        // Clear the calculator so if they back out, the pin is gone
        clear()
    }
}
