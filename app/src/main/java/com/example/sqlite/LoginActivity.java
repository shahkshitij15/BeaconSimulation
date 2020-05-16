package com.example.sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText e1,e2;
    Button b1;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db=new DatabaseHelper(this);
        e1=(EditText)findViewById(R.id.email1);
        e2=(EditText)findViewById(R.id.pass1);
        b1=(Button)findViewById(R.id.login1);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = e1.getText().toString();
                String password = e2.getText().toString();
                Boolean login = db.login(email,password);
                if(login==true){
                    startActivity(new Intent(LoginActivity.this,BeaconDetection.class));
                }
                else{
                    Toast.makeText(getApplicationContext(),"Try Again Mate!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
