package com.example.ahmedsayed.privatechat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;

    private ViewPager mViewPager;
    private PagerAdaper mPagerAdaper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mViewPager= (ViewPager) findViewById(R.id.main_tabpager);
        mPagerAdaper =new PagerAdaper(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdaper);

        mTabLayout= (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mToolbar= (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Private App");
        if(mAuth.getCurrentUser()!=null){
            mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser =mAuth.getCurrentUser();
        if(currentUser==null)
        {
            senToStart();
        }else
        {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser =mAuth.getCurrentUser();
        if(currentUser!=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void senToStart() {
        Intent intent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_btn)
        {
            FirebaseUser currentUser =mAuth.getCurrentUser();
            if(currentUser!=null) {
                mUserRef.child("online").setValue(false);
            }
            FirebaseAuth.getInstance().signOut();
            senToStart();

        }
        if(item.getItemId()==R.id.main_settings_btn)
        {
            Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.main_all_user_btn)
        {
            Intent intent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }
         return true;
    }
}
