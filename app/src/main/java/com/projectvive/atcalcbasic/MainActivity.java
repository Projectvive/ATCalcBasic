package com.projectvive.atcalcbasic;

import android.speech.tts.TextToSpeech;
import android.support.v4.widget.TextViewCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,  TextToSpeech.OnInitListener{

    double operand1 = 0.0;
    double operand2 = 0.0;
    double result = 0.0;
    String operand1String = "0";
    String operand2String = "";
    String operatorString = "";
    boolean operand_one_active = true;
    boolean waitingForOperand = true;
    ImageButton backButton, eraseButton;

    TextToSpeech tts;

    TextView inputBox;
    Button btn[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputBox = findViewById(R.id.input);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(inputBox, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        tts = new TextToSpeech(this, this);

        btn = new Button[17];
        btn[0] = findViewById(R.id.num0);
        btn[1] = findViewById(R.id.num1);
        btn[2] = findViewById(R.id.num2);
        btn[3] = findViewById(R.id.num3);
        btn[4] = findViewById(R.id.num4);
        btn[5] = findViewById(R.id.num5);
        btn[6] = findViewById(R.id.num6);
        btn[7] = findViewById(R.id.num7);
        btn[8] = findViewById(R.id.num8);
        btn[9] = findViewById(R.id.num9);
        btn[10] = findViewById(R.id.symplus);
        btn[11] = findViewById(R.id.symmin);
        btn[12] = findViewById(R.id.symmulti);
        btn[13] = findViewById(R.id.symdiv);
        btn[14] = findViewById(R.id.symequ);
        btn[15] = findViewById(R.id.symdot);
        btn[16] = findViewById(R.id.sympulmi);


        backButton = findViewById(R.id.symback);
        backButton.setOnClickListener(this);
        eraseButton = findViewById(R.id.symera);
        eraseButton.setOnClickListener(this);

        for (int i = 0; i < 17; i++) {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(btn[i], TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            btn[i].setOnClickListener(this);
        }
    }

    public void digitClicked(int digitValue) {
        if(operand_one_active) {
            if(operand1String == "0" && digitValue == 0.0) {
                return;
            }
            if(waitingForOperand) {
                operand1String = "";
                waitingForOperand = false;
            }
            operand1String += String.valueOf(digitValue);
            speakOut(operand1String);
        }
        else {
            operand2String += String.valueOf(digitValue);
            speakOut(operand2String);
        }

        updateDisplay();
    }

    void backspaceClicked() {
        if(waitingForOperand) {
            clearAll();
        } else if(operand1String != "" && operatorString != "" && operand2String != "") {
            operand2String = operand2String.substring(0, operand2String.length() - 1);
        } else if(operand1String != "" && operatorString != "") {
            operatorString = "";
            operand_one_active = true;
        } else if(operand1String != "0" && operand1String != "") {
            operand1String = operand1String.substring(0, operand1String.length() - 1);
        } else {
            clearAll();
        }
        if(operand1String.isEmpty()) {
            clearAll();
        }
        updateDisplay();
    }

    void clearAll() {
        result = 0.0;
        operand1 = 0.0;
        operand2 = 0.0;
        operand1String = "0";
        operand2String = "";
        operatorString = "";
        waitingForOperand = true;
        operand_one_active = true;
        updateDisplay();
    }

    boolean calculate( String preText) {
        double tempResult = 0.0;
        if (operand1String != "" && operatorString != "" && operand2String != "") {
            operand1 = Double.valueOf(operand1String);
            operand2 = Double.valueOf(operand2String);
            if (operatorString == getResources().getString(R.string.symbol_plus)) {
                result = operand1 + operand2;
            } else if (operatorString == getResources().getString(R.string.symbol_minus)) {
                result = operand1 - operand2;
            } else if (operatorString == getResources().getString(R.string.symbol_multiple)) {
                result = operand1 * operand2;
            } else if (operatorString == getResources().getString(R.string.symbol_divide)) {
                if (operand2 == 0.0) {
                    speakOut("Cannot divide by 0");
                    abortOperation();
                    return false;
                }
                result = operand1 / operand2;
            } else {
                return false;
            }
            tempResult = result;

            clearAll();
            if (tempResult % 1 == 0) {
                operand1String = String.valueOf((int)tempResult);

            } else {
                double temp = tempResult - (int)tempResult;
                if (String.valueOf(temp).length() < 8) {
                    operand1String = String.valueOf(tempResult);
                } else {
                    operand1String = String.valueOf((int)tempResult) + String.valueOf(temp).substring(1, 7);
                }
            }

            waitingForOperand = true;
            updateDisplay();
            speakOut("Equals " + operand1String);
        } else {
            return false;
        }

        return true;
    }

    void multiplicativeOperatorClicked(int val) {
        String clickedOperator = "";
        if (val == 1) {
            clickedOperator = getResources().getString(R.string.symbol_multiple);
        } else if (val == 2) {
            clickedOperator = getResources().getString(R.string.symbol_divide);
        }
        if(operand_one_active && operand2String == "") {
            operatorString = clickedOperator;
            operand_one_active = false;
            updateDisplay();
        } else if (operatorString != "" && operand2String != "") {
            calculate("");
            waitingForOperand = false;
            operatorString = clickedOperator;
            operand_one_active = false;
            updateDisplay();
        } else if (operatorString != "" && operand2String == "") {
            operatorString = clickedOperator;
            updateDisplay();
        } else {
            abortOperation();
        }
        if (operatorString == getResources().getString(R.string.symbol_multiple)) {
            speakOut("times");
        } else if (operatorString == getResources().getString(R.string.symbol_divide)) {
            speakOut("divide by");
        }
        return;
    }

    void additiveOperatorClicked(int val) {
        String clickedOperator = "";
        if (val == 1) {
            clickedOperator = getResources().getString(R.string.symbol_plus);
        } else if (val == 2) {
            clickedOperator = getResources().getString(R.string.symbol_minus);
        }

        if(operand_one_active && operand2String == "") {
            operatorString = clickedOperator;
            operand_one_active = false;
            updateDisplay();
        } else if (operatorString != "" && operand2String != "") {
            calculate("");
            waitingForOperand = false;
            operatorString = clickedOperator;
            operand_one_active = false;
            updateDisplay();
        } else if (operatorString != "" && operand2String == "") {
            operatorString = clickedOperator;
            updateDisplay();
        } else {
            abortOperation();
        }
        if (clickedOperator == getResources().getString(R.string.symbol_plus)) {
            speakOut("Plus");
        } else if(clickedOperator == getResources().getString(R.string.symbol_minus)) {
            speakOut("Minus");
        }
        return;
    }


    void updateDisplay() {
        if(operand1String != "" && operand2String != "" && operatorString != "") {
            inputBox.setText(operand1String + " " + operatorString + " " + operand2String);
        } else if (operand1String != "" && operatorString != "") {
            inputBox.setText(operand1String + " " + operatorString);
        } else if (operand1String != "") {
            inputBox.setText(operand1String);
        } else {
            inputBox.setText("ERR");
        }

    }

    void pointClicked() {
        if(operand_one_active) {
            if(waitingForOperand) {
                operand1String = "0";
                waitingForOperand = false;
            }
            if(!operand1String.contains(".")) {
                operand1String += ".";
            }
        } else {
            if (!operand2String.contains(".")) {
                operand2String += ".";
            }
        }
        waitingForOperand = false;
        updateDisplay();
    }

    void equalClicked() {
        if(operand1String != "" && operatorString != "" && operand2String != "") {
            calculate("Equals ");
        } else {
            abortOperation();
        }
    }

    void abortOperation() {
        clearAll();
        inputBox.setText("####");
    }

    void changeSignClicked() {
        String text = "";
        double value = 0.0;
        if(operand_one_active) {
            text = operand1String;
            value = Double.valueOf(text);
            if (value > 0.0) {
                text = "-" + text;
            } else if (value < 0.0) {
                text = text.substring(1, text.length());
            }
            operand1String = text;
            speakOut(operand1String);
        } else {
            text = operand2String;
            value = Double.valueOf(text);
            if (value > 0.0) {
                text = "-" + text;
            } else if (value < 0.0) {
                text = text.substring(1, text.length());
            }
            operand2String = text;
            speakOut(operand2String);
        }
        updateDisplay();
    }




    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.num0) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_zero)));
        } else if (view.getId() == R.id.num1) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_one)));
        } else if (view.getId() == R.id.num2) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_two)));
        } else if (view.getId() == R.id.num3) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_three)));
        } else if (view.getId() == R.id.num4) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_four)));
        } else if (view.getId() == R.id.num5) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_five)));
        } else if (view.getId() == R.id.num6) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_six)));
        } else if (view.getId() == R.id.num7) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_seven)));
        } else if (view.getId() == R.id.num8) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_eight)));
        } else if (view.getId() == R.id.num9) {
            digitClicked(Integer.valueOf(getResources().getString(R.string.num_nine)));
        } else if (view.getId() == R.id.symplus) {
            additiveOperatorClicked(1);
        } else if (view.getId() == R.id.symmin) {
            additiveOperatorClicked(2);
        } else if (view.getId() == R.id.symmulti) {
            multiplicativeOperatorClicked(1);
        } else if (view.getId() == R.id.symdiv) {
            multiplicativeOperatorClicked(2);
        } else if (view.getId() == R.id.symequ) {
            equalClicked();
        } else if (view.getId() == R.id.symera) {
            clearAll();
        } else if (view.getId() == R.id.symback) {
            backspaceClicked();
        } else if (view.getId() == R.id.symdot) {
            pointClicked();
        } else if (view.getId() == R.id.sympulmi) {
            changeSignClicked();
        }


    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            // tts.setPitch(5); // set pitch level

            // tts.setSpeechRate(2); // set speech speed rate

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed");
        }

    }

    private void speakOut(String text) {

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
