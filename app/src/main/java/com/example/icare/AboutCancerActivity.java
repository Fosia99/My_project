package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AboutCancerActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_cancer);

        mToolbar = (Toolbar) findViewById(R.id.about_app_bar);
        setSupportActionBar(mToolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("About Breast Cancer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(AboutCancerActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.navigation_about);
        View navView = navigationView.inflateHeaderView(R.layout.hearder2);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);
                return false;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.about_prevention:
                Toast.makeText(this, "Preventative measures", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_treatment:
                Toast.makeText(this, "Treatment measures", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_signs:
                Toast.makeText(this, "Signs", Toast.LENGTH_SHORT).show();
                break;

            case R.id.about_self_exam:
                Toast.makeText(this, "Examination Tips", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_risk_factors:
                Toast.makeText(this, "Risk Factors", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_more_info:
                Toast.makeText(this, "More Information", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_get_involved:
                Toast.makeText(this, "Get Involved", Toast.LENGTH_SHORT).show();
                break;


        }
    }
}