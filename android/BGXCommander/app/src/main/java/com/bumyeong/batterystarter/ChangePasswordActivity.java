package com.bumyeong.batterystarter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText mInputPassword;
    private EditText mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mInputPassword = (EditText)findViewById(R.id.editDevicePassword);
        mConfirmPassword = (EditText)findViewById(R.id.editDevicePasswordConfirm);

        Button btnOK = (Button)findViewById(R.id.btnPasswordOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputPassword = mInputPassword.getText().toString().trim();
                String confirmPassword = mConfirmPassword.getText().toString().trim();

                if( inputPassword.equals(confirmPassword) == true ) {
                    Intent intent = new Intent();
                    intent.putExtra("change_password", inputPassword);

                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    ShowAlertDialog("The passwords you entered are different.\n\nPlease enter it correctly.");
                }
            }
        });

    }

    private void ShowAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

        builder.setTitle("ERROR PASSWORD");
        builder.setMessage(message);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();
    }
}