package com.example.ahmedsayed.privatechat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private RecyclerView mRequestsList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mRequestDatabase;
    private FirebaseAuth mAuth;
    private View mMainView;
    private FirebaseUser mCurrentUser;

    public RequestFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser= mAuth.getCurrentUser();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestDatabase=FirebaseDatabase.getInstance().getReference().child("request_list");

        mRequestsList= (RecyclerView) mMainView.findViewById(R.id.request_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(linearLayoutManager);


        return mMainView;

    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,RequsetsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users,RequsetsViewHolder>(
                Users.class,
                R.layout.request_single_layout,
               RequsetsViewHolder.class,
                mRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequsetsViewHolder viewHolder, final Users model, final int position) {

                mRequestDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot usersSnapshot = dataSnapshot.child(mCurrentUser.getUid());
                        Iterable<DataSnapshot> userChildren = usersSnapshot.getChildren();

                        for (DataSnapshot contact : userChildren) {
                            final String senderUser = contact.getKey();
                            Log.d("sender user id",senderUser);
                            mUserDatabase.child(senderUser).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // final String user_id=getRef(position).getKey();
                                    String display_name=dataSnapshot.child("name").getValue().toString();
                                    String status=dataSnapshot.child("status").getValue().toString();
                                    String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                                    viewHolder.setName(display_name);
                                    viewHolder.setStatus(status);
                                    viewHolder.setThumbImage(thumb_image,getContext());
                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent=new Intent(getContext(),ProfileActivity.class);
                                            intent.putExtra("user_id",senderUser);
                                            startActivity(intent);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        mRequestsList.setAdapter(firebaseRecyclerAdapter);
    }



    public static class RequsetsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Button acceptBtn,cancelBtn;
        public RequsetsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            acceptBtn= (Button) itemView.findViewById(R.id.accept_request_btn);
            cancelBtn= (Button) itemView.findViewById(R.id.cancel_request_btn);
        }
        public void setName(String name)
        {
            TextView userNameView= (TextView) mView.findViewById(R.id.request_user_name);
            userNameView.setText(name);
        }
        public void setStatus(String status)
        {
            TextView userStatusView= (TextView) mView.findViewById(R.id.request_single_status);
            userStatusView.setText(status);
        }
        public void setThumbImage(String thumb_image, Context context)
        {
            CircleImageView userimageview= (CircleImageView) mView.findViewById(R.id.request_user_img);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.default_avatar).into(userimageview);

        }
    }

}
