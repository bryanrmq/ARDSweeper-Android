package fr.fliizweb.ardsweeper.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class SplashScreenActivity extends Activity implements View.OnClickListener {

    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRegister = (Button) findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Class nameActivity = null; // Declare class null

        // For each button, we send the right activity
        if(view == btnLogin)
            nameActivity = LoginActivity.class;
        else if(view == btnRegister)
            nameActivity = RegisterActivity.class;


        Intent it = new Intent(SplashScreenActivity.this, nameActivity);
        SplashScreenActivity.this.startActivity(it);
        finish();
    }
}
