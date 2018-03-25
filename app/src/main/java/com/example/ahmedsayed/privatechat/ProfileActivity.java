package com.example.ahmedsayed.privatechat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mProfileUserImage;
    private TextView mProfileDisplayName;
    private TextView mProfileUserStatus;
    private TextView mProfileTotalFriends;
    private Button mProfileSendFrienRequest;
    private Button mProfileDeclineFriendRequest;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private DatabaseReference mRequestList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;


    private String current_state;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        final String user_id=  getIntent().getStringExtra("user_id");

        mRootRef=FirebaseDatabase.getInstance().getReference();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mRequestList=FirebaseDatabase.getInstance().getReference().child("request_list");
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        mProfileUserImage= (ImageView) findViewById(R.id.profile_user_image);
        mProfileDisplayName= (TextView) findViewById(R.id.profile_user_name);
        mProfileTotalFriends= (TextView) findViewById(R.id.profile_user_totalfriend);
        mProfileUserStatus= (TextView) findViewById(R.id.profile_user_status);
        mProfileSendFrienRequest= (Button) findViewById(R.id.profile_send_request);
        mProfileDeclineFriendRequest= (Button) findViewById(R.id.profile_decline_request);

        mProfileDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map unfriendMap = new HashMap();
                unfriendMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                unfriendMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);

                mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                        if(databaseError == null){

                            current_state = "not_friends";
                            mProfileSendFrienRequest.setText("Send Friend Request");

                            mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                            mProfileDeclineFriendRequest.setEnabled(false);

                        } else {

                            String error = databaseError.getMessage();

                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                        }

                        mProfileSendFrienRequest.setEnabled(true);

                    }
                });
            }
        });


        current_state ="not_friends";
        mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
        mProfileDeclineFriendRequest.setEnabled(false);

        progressDialog=new ProgressDialog(ProfileActivity.this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we Loading User Data ");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Log.d("userIdPassed",user_id);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileDisplayName.setText(display_name);
                mProfileUserStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileUserImage);
                if(mCurrentUser.getUid().equals(user_id)){

                    mProfileDeclineFriendRequest.setEnabled(false);
                    mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);

                    mProfileSendFrienRequest.setEnabled(false);
                    mProfileSendFrienRequest.setVisibility(View.INVISIBLE);

                }
                //-----------Friend Request Feature----------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                        mProfileDeclineFriendRequest.setEnabled(false);
                        if (dataSnapshot.hasChild(user_id))
                        {
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received"))
                            {
                                current_state="req_received";
                                mProfileSendFrienRequest.setText("Accept Friend Request");
                                mProfileDeclineFriendRequest.setVisibility(View.VISIBLE);
                                mProfileDeclineFriendRequest.setEnabled(true);

                            }else if (req_type.equals("sent"))
                            {
                                current_state="req_sent";
                                mProfileSendFrienRequest.setText("Cancle Friend Request");
                                mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineFriendRequest.setEnabled(false);

                            }
                            progressDialog.dismiss();
                        }  else
                        {
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        current_state="friend";
                                        mProfileSendFrienRequest.setText("Unfriend This Person");
                                        mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                        mProfileDeclineFriendRequest.setEnabled(false);
                                    }
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendFrienRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendFrienRequest.setEnabled(false);
                //-----------------------not frind state------------
                if(current_state.equals("not_friends"))
                {
                    DatabaseReference newNotificationref=mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId=newNotificationref.getKey();

                    HashMap<String,String> notificationData=new HashMap();
                    notificationData.put("fromuser",mCurrentUser.getUid());
                    notificationData.put("type","request");


                    Map requestMap=new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type","received");
                    requestMap.put("request_list/"+ user_id + "/" + mCurrentUser.getUid() + "/type","received");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError !=null){
                               Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_LONG).show();
                            }else{
                            mProfileSendFrienRequest.setEnabled(true);
                            current_state="req_sent";
                            mProfileSendFrienRequest.setText("Cancle Friend Request");}

                        }
                    });
                }
                //--------------------- cancle request state -----------------------------
                if(current_state.equals("req_sent"))
                {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                          mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  mNotificationDatabase.child(user_id).removeValue();
                                  mProfileSendFrienRequest.setEnabled(true);
                                  current_state="not_friends";
                                  mProfileSendFrienRequest.setText("send Friend Request");

                                  mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                  mProfileDeclineFriendRequest.setEnabled(false);
                              }
                          });
                        }
                    });
                }
                //----------------- Req Received State----------------
                if(current_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrentUser.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mProfileSendFrienRequest.setEnabled(true);
                                current_state = "friends";
                                mProfileSendFrienRequest.setText("Unfriend this Person");

                                mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineFriendRequest.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }
                        }
                    });
                }
                //------------------ unfriend this person ---------------------
                if(current_state.equals("friend"))
                {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                current_state = "not_friends";
                                mProfileSendFrienRequest.setText("Send Friend Request");

                                mProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                mProfileDeclineFriendRequest.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            mProfileSendFrienRequest.setEnabled(true);

                        }
                    });

                }


            }
        });


    }

}
