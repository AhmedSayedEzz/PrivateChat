package com.example.ahmedsayed.privatechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    private TextView setting_display_name;
    private TextView setting_status;
    private CircleImageView setting_image;
    private Button setting_change_status_btn;
    private Button setting_change_image_btn;
    private static final int GALLERY_PICK = 1;

    //srorage firebase
    private StorageReference mImageStorage;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        setting_display_name= (TextView) findViewById(R.id.setting_display_name);
        setting_status= (TextView) findViewById(R.id.setting_status);
        setting_image= (CircleImageView) findViewById(R.id.settings_image);

        mImageStorage= FirebaseStorage.getInstance().getReference();

        setting_change_image_btn= (Button) findViewById(R.id.setting_img_btn);


        setting_change_status_btn= (Button) findViewById(R.id.setting_status_btn);
        setting_change_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value=setting_status.getText().toString();
                Intent intent=new Intent(SettingsActivity.this,StatusActivity.class);
                intent.putExtra("status_value",status_value);
                startActivity(intent);
            }
        });
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=mCurrentUser.getUid();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                setting_display_name.setText(name);
                setting_status.setText(status);
                if(!image.equals("default")) {
                   // Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(setting_image);

                    // get image offline
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(setting_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            //if image not loaded offline
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(setting_image);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setting_change_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        /* CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .getIntent(SettingsActivity.this);*/

                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK&&resultCode==RESULT_OK)
        {
            Uri imageUri=data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(SettingsActivity.this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog=new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while we Uploading and process the image ");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath=new File(resultUri.getPath());
                String current_user_id=mCurrentUser.getUid();

                try {
                    final Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(60)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                   final byte[] thumb_byte = baos.toByteArray();



                StorageReference filepath=mImageStorage.child("profile_images").child( current_user_id+".jpg");
                final StorageReference thumb_image_filepath=mImageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                           final String download_url=task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_image_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot>thumb_task) {
                                    String thumb_download_url=thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                       Map update_hashmap=new HashMap();
                                        update_hashmap.put("image",download_url);
                                        update_hashmap.put("thumb_image",thumb_download_url);
                                        mUserDatabase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Success Uploading", Toast.LENGTH_LONG).show();

                                                }
                                            }
                                        });
                                    }else
                                    {
                                        Toast.makeText(SettingsActivity.this, "Error in Uploading thumb Image", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }

                                }
                            });



                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Error in Uploading", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
                } catch (IOException e) {
                    Toast.makeText(SettingsActivity.this, "Bitmap Error", Toast.LENGTH_LONG).show();

                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
