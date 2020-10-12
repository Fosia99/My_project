package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText EnterEmail;
    private Button ButtonResetOk;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        EnterEmail = (EditText) findViewById(R.id.text_email);
        ButtonResetOk= (Button) findViewById(R.id.Send_button);
        mAuth= FirebaseAuth.getInstance();


        mToolbar = (Toolbar) findViewById(R.id.forgot_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");


        ButtonResetOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userEmail = EnterEmail.getText().toString();
                if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                }
                else {
                     mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             
                             if(task.isSuccessful())
                             {
                                 Toast.makeText(ResetPasswordActivity.this, "Link send successfully, please check your emails", Toast.LENGTH_SHORT).show();
                                 startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));

                             }
                             else {   String message = task.getException().getMessage();
                                 Toast.makeText(ResetPasswordActivity.this, "Oops,Error Occurred " + message, Toast.LENGTH_SHORT).show();
                             }

                         }
                     });
                }
            }



        });

    }
}