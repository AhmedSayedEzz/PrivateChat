package com.example.ahmedsayed.privatechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreatAccount;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private Toolbar mToolbar;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar= (Toolbar) findViewById(R.id.register_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog=new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mDisplayName= (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail =(TextInputLayout) findViewById(R.id.reg_email);
        mPassword=(TextInputLayout) findViewById(R.id.reg_password);
        mCreatAccount= (Button) findViewById(R.id.reg_create_account);

        mCreatAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name=mDisplayName.getEditText().getText().toString();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();
                if(!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))
                {
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create you account !");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    register_user(display_name,email,password);
                }

            }
        });
    }
    private void register_user(final String display_name, String email, String password)
    {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {

                    FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                    String uID=currentUser.getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("name",display_name);
                    userMap.put("status","Hi there ,i'm useing Private Chat app");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("device_token",deviceToken);
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });


                }
            else{
                    progressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "can't sign in ,please check the form and try again. ", Toast.LENGTH_LONG).show();

                }
            }
        });
    }
}
