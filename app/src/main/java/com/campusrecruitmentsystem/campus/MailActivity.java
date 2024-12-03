package com.campusrecruitmentsystem.campus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.dhruv.campusrecruitmentsystem.R;

public class MailActivity extends AppCompatActivity {
    EditText editTextTo, editTextSubject, editTextMessage;
    Button send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);
        editTextTo = (EditText) findViewById(R.id.to);
        editTextSubject = (EditText) findViewById(R.id.sub);
        editTextMessage = (EditText) findViewById(R.id.msg);
        send = (Button) findViewById(R.id.btnsend);
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String to = editTextTo.getText().toString();
                String subject = editTextSubject.getText().toString();
                String message = editTextMessage.getText().toString();
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                email.putExtra(Intent.EXTRA_TEXT, message);
//need this to prompts email client only
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));
            }
        });
    }
}