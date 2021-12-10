package com.e.aucrypto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

public class splash extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //aftersplashscreen i.e. after launching ap update theme by Apptheme
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(splash.this, MainActivity.class);
                splash.this.startActivity(mainIntent);
                splash.this.finish();   //so after back when comehere again activity finished


                //tip->on last activity call finish before starting new so that it dont add in stack when go to prev activity using button other than back
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
