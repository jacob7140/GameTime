package com.example.gametime;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements CreateAccountFragment.RegisterListener, OpeningFragment.OpeningListner, LoginFragment.LoginListener, HomeFragment.HomeListener, CreateGameFragment.CreateGameListener {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.rootView, new OpeningFragment())
                    .commit();
        } else{
            getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new HomeFragment()).commit();
        }
    }

    @Override
    public void gotoCreateAccount() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.rootView, new CreateAccountFragment())
                .commit();
    }

    @Override
    public void gotoCreateGame() {
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new CreateGameFragment()).commit();
    }

    @Override
    public void gotoFindGame() {

    }

    @Override
    public void gotoLogin() {
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new LoginFragment()).commit();
    }

    @Override
    public void gotoHome() {
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new HomeFragment()).commit();
    }

    @Override
    public void gotoOpening() {
        getSupportFragmentManager().beginTransaction().replace(R.id.rootView, new OpeningFragment()).commit();
    }
}