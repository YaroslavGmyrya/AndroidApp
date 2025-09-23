package com.example.calculator.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.R

class CalculatorActivity : AppCompatActivity() {

    //declaration

    //TextViews
    private lateinit var prevExpression : TextView
    private lateinit var expression : TextView
    //numbers
    private lateinit var zero : Button
    private lateinit var doubleZero : Button
    private lateinit var one : Button
    private lateinit var two : Button
    private lateinit var three : Button
    private lateinit var four : Button
    private lateinit var five : Button
    private lateinit var six : Button
    private lateinit var seven : Button
    private lateinit var eight : Button
    private lateinit var nine : Button
    //operations
    private lateinit var sum : Button
    private lateinit var minus : Button
    private lateinit var div : Button
    private lateinit var mod : Button
    private lateinit var multiplication : Button
    private lateinit var result : Button
    //other button
    private lateinit var BackSpace : Button
    private lateinit var Clear : Button
    private lateinit var dot : Button
    //Arrays
    private lateinit var numbers: Array<Button>
    private lateinit var operations: Array<Button>

    //File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //initialization

        //TextView
        expression = findViewById(R.id.expression);
        prevExpression = findViewById(R.id.prevExpression);

        //operation
        sum = findViewById(R.id.sum);
        minus = findViewById(R.id.minus);
        div = findViewById(R.id.div);
        mod = findViewById(R.id.Mod);
        multiplication = findViewById(R.id.multiplication);

        //Button with number
        zero = findViewById(R.id.zero);

        doubleZero = findViewById(R.id.doubleZero);

        one = findViewById(R.id.one);

        two = findViewById(R.id.two);

        three = findViewById(R.id.three);

        four = findViewById(R.id.four);

        five = findViewById(R.id.five);

        six = findViewById(R.id.six);

        seven = findViewById(R.id.seven);

        eight = findViewById(R.id.eight);

        nine = findViewById(R.id.nine);

        //otherButton
        dot = findViewById(R.id.dot);

        result = findViewById(R.id.Result);

        Clear = findViewById(R.id.Clear);

        BackSpace = findViewById(R.id.BackSpace);


        //arrays
        numbers = arrayOf(zero, doubleZero, one, two, three, four, five, six, seven, eight, nine);

        operations = arrayOf(sum, minus, div, mod, multiplication);
    }

    override fun onResume() {
        super.onResume()

        for(el in numbers)
            el.setOnClickListener({
                expression.setText(expression.text.toString() + el.text.toString());
            })

        for(el in operations)
            el.setOnClickListener({
                if(!expression.text.toString().isEmpty()){
                    if(expression.text.toString().last().isDigit())
                        expression.setText(expression.text.toString() + " " + el.text.toString() + " ");
                }

                else
                    expression.setText(expression.text)
            })

        dot.setOnClickListener({
            val split_expression = expression.text.toString().split(" ");
            if(!split_expression[split_expression.size - 1].contains("."))
                expression.setText(expression.text.toString() + ".")
        })

        result.setOnClickListener({
            //split expression on operands and operator
            val splitExpression = expression.text.toString().split(" ");
            if(splitExpression.size == 3){
                val first_operand = splitExpression[0].toDoubleOrNull();
                val operator = splitExpression[1];
                val second_operand = splitExpression[2].toDoubleOrNull()

                if(first_operand != null && second_operand != null){
                    //update prevExpression TextView
                    prevExpression.setText(expression.text);
                    //choice operand
                    when(operator){
                        "+" -> {expression.setText((first_operand + second_operand).toString())};
                        "-" -> {expression.setText((first_operand - second_operand).toString())};
                        "%" -> {expression.setText((first_operand % second_operand).toString())};
                        "/" -> {expression.setText((first_operand / second_operand).toString())};
                        "X" -> {expression.setText((first_operand * second_operand).toString())}
                    }
                }

            }
        })

        Clear.setOnClickListener({
            expression.setText("");
            prevExpression.setText("");
        })

        BackSpace.setOnClickListener({
            expression.setText(expression.text.toString().dropLast(1))
        })

    }

}