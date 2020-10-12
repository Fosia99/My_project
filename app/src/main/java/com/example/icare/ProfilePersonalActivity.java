package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePersonalActivity extends AppCompatActivity {
        private TextView profileStatus,profileName,profileUserName,profileDob,profileGender,
                profilePhoneNumber,profileType,profileRelationshipStatus,profileCountry,profileInterests;
        private CircleImageView profileImage;
        private DatabaseReference ProfileUserRef,UserRef,FriendRequestRef,FriendsRef;
        private Button SendRequest,DeclineRequest;

        private FirebaseAuth mAuth;
      private   String RecieverUserId,senderUserId, CurrentState;
        private ProgressDialog loadingBar;
        final static int Gallery_Pick = 1;
        private StorageReference UserProfileImageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_personal);

        mAuth = FirebaseAuth.getInstance();
        RecieverUserId = getIntent().getExtras().get("clicked_user_id").toString();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        senderUserId = mAuth.getCurrentUser().getUid();

        SendRequest = (Button) findViewById(R.id.send_friend_request);
        DeclineRequest = (Button) findViewById(R.id.decline_friend_request);

        CurrentState = "Not Friends";

        profileStatus = (TextView) findViewById(R.id.personal_profile_status);
        profileName = (TextView) findViewById(R.id.personal_profile_Name);
        profileUserName = (TextView) findViewById(R.id.personal_profile_userName);
        profileDob = (TextView) findViewById(R.id.personal_profile_dob);
        profileGender = (TextView) findViewById(R.id.personal_profile_gender);
        profilePhoneNumber = (TextView) findViewById(R.id.personal_profile_phone_number);
        profileType = (TextView) findViewById(R.id.personal_profile_User_type);
        profileRelationshipStatus = (TextView) findViewById(R.id.personal_profile_relationship_status);
        profileCountry = (TextView) findViewById(R.id.personal_profile_country);
        profileInterests = (TextView) findViewById(R.id.personal_profile_interests);
        profileImage = (CircleImageView) findViewById(R.id.personal_profile_Image);

        DeclineRequest.setVisibility(View.INVISIBLE);
        DeclineRequest.setEnabled(false);


        if (!senderUserId.equals(RecieverUserId)) {
            SendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SendRequest.setEnabled(false);
                    if (CurrentState.equals("Not Friends")) {
                        SendFriendRequest();
                    }

                    if (CurrentState.equals("request_sent")) {
                        CancelFriendRequest();
                    }

                    if (CurrentState.equals("request_received")) {

                        AcceptRequest();
                    }
                    if (CurrentState.equals("friends")) {

                        UnFriendAFriend();}
                }
            });

        }
        else {
            DeclineRequest.setVisibility(View.INVISIBLE);
            SendRequest.setVisibility(View.INVISIBLE);
        }
        UserRef.child(RecieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String myProImage = snapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(myProImage).placeholder(R.drawable.profile).into(profileImage);

                    String myUserName = snapshot.child("username").getValue().toString();
                    profileUserName.setText(myUserName);

                    String myFullName = snapshot.child("fullname").getValue().toString();
                    profileName.setText(myFullName);

                    String myUserProStatus = snapshot.child("profilestatus").getValue().toString();
                    profileStatus.setText(myUserProStatus);

                    String myUserDob = snapshot.child("dateofbirth").getValue().toString();
                    profileDob.setText(myUserDob);

                    String myUserGender = snapshot.child("gender").getValue().toString();
                    profileGender.setText(myUserGender);

                    String myUserPhoneNumber = snapshot.child("phonenumber").getValue().toString();
                    profilePhoneNumber.setText(myUserPhoneNumber);

                    String myUserType = snapshot.child("usertype").getValue().toString();
                    profileType.setText(myUserType);

                    String myUserRelationshipStatus = snapshot.child("relationshipstatus").getValue().toString();
                    profileRelationshipStatus.setText(myUserRelationshipStatus);

                    String myUserCountry = snapshot.child("country").getValue().toString();
                    profileCountry.setText(myUserCountry);

                    String myUserInterests = snapshot.child("interest").getValue().toString();
                    profileInterests.setText(myUserInterests);

                    MaintainanceofButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void UnFriendAFriend() {

        FriendsRef.child(senderUserId).child(RecieverUserId).child("request_type").removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            FriendsRef.child(RecieverUserId).child(senderUserId).child("request_type").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                SendRequest.setEnabled(true);
                                                CurrentState = "Not Friends";
                                                SendRequest.setText("+ Follow");
                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });




    }

    private void AcceptRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsRef.child(senderUserId).child(RecieverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            FriendsRef.child(RecieverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            if (task.isSuccessful()) {

                                                FriendRequestRef.child(senderUserId).child(RecieverUserId).child("request_type").removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    FriendRequestRef.child(RecieverUserId).child(senderUserId).child("request_type").removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()) {
                                                                                        SendRequest.setEnabled(true);
                                                                                        CurrentState = "friends";
                                                                                        SendRequest.setText("UnFollow");
                                                                                        DeclineRequest.setVisibility(View.INVISIBLE);
                                                                                        DeclineRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void CancelFriendRequest() {

        FriendRequestRef.child(senderUserId).child(RecieverUserId).child("request_type").removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            FriendRequestRef.child(RecieverUserId).child(senderUserId).child("request_type").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                SendRequest.setEnabled(true);
                                                CurrentState = "Not Friends";
                                                SendRequest.setText("+Follow");
                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }


    private void MaintainanceofButtons() {

        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(RecieverUserId))
                {
                    String request_type =snapshot.child(RecieverUserId).child("request_type").getValue().toString();

                if (request_type.equals("sent")) {
                    CurrentState = "request_sent";
                    SendRequest.setText("Cancel Request");
                    DeclineRequest.setVisibility(View.INVISIBLE);
                    DeclineRequest.setEnabled(false);

                }
                else if(request_type.equals("received"))
                {
                    CurrentState = "request_received";
                    SendRequest.setText("Accept Request");
                    DeclineRequest.setVisibility(View.VISIBLE);
                    DeclineRequest.setEnabled(true);

                    DeclineRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            CancelFriendRequest();
                        }
                    });
                }
                else
                {
                    FriendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(RecieverUserId))
                            {
                                CurrentState = "friends";
                                SendRequest.setText("Unfollow");
                                DeclineRequest.setVisibility(View.INVISIBLE);
                                DeclineRequest.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void SendFriendRequest() {

FriendRequestRef.child(senderUserId).child(RecieverUserId).child("request_type").setValue("sent")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {

        if(task.isSuccessful())
        {
            FriendRequestRef.child(RecieverUserId).child(senderUserId).child("request_type").setValue("received")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                SendRequest.setEnabled(true);
                                CurrentState = "request_sent";
                                SendRequest.setText("Cancel request");
                                DeclineRequest.setVisibility(View.INVISIBLE);
                                DeclineRequest.setEnabled(false);
                            }
                        }
                    });
        }




    }
});

    }
}