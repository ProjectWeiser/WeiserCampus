package com.example.shounakk.utdallas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button button = (Button) findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(mainActivityIntent);
            }
        });
    }
}