package com.example.icare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText chatMessageContent;
    private ImageButton sendImage,sendMsgButton;
    private RecyclerView  UserMessageList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private  MessagesAdapter messageAdapter;

    private  String  msgReceiverId,msgReceiverName,msgSenderId;
    private FirebaseAuth mAuth;
    private TextView ReceiverUserName,UserLastSeen;
    private CircleImageView ReceiverUserImage;
    private DatabaseReference RootRef,UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        Initialize();


        DisplayReceiverInfo();

        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        FetchMessages();

    }

    private void FetchMessages() {
        RootRef.child("Messages").child(msgSenderId).child(msgReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if(snapshot.exists())
                {
                    Messages messages = snapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messageAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        msgSenderId = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        msgReceiverId = getIntent().getExtras().get("clicked_user_id").toString();
        msgReceiverName = getIntent().getExtras().get("userName").toString();
        ReceiverUserName = (TextView) findViewById(R.id.custom_profile_name);
        UserLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        ReceiverUserImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendImage = (ImageButton) findViewById(R.id.chat_image);
        sendMsgButton = (ImageButton) findViewById(R.id.chat_send_button);
        chatMessageContent = (EditText) findViewById(R.id.chat_message_description);

        messageAdapter = new MessagesAdapter(messagesList);

        UserMessageList = (RecyclerView) findViewById(R.id.messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        UserMessageList.setHasFixedSize(true);
       UserMessageList.setLayoutManager(linearLayoutManager);
        UserMessageList.setAdapter(messageAdapter);
    }

    private void SendMessage() {

        updateUserStatus("online");

        String messageText= chatMessageContent.getText().toString();
            if (TextUtils.isEmpty(messageText))
            {
                Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show();
            }

            else
                {
                   String sender_message_Ref =   msgSenderId + "/" + msgReceiverId;
                   String receiver_message_Ref =   msgReceiverId + "/" + msgSenderId;
                   DatabaseReference user_message_key = RootRef.child("Messages").child(msgSenderId)
                           .child(msgReceiverId).push();

                   String message_push_id = user_message_key.getKey();


                    Calendar calForDate = Calendar.getInstance();
                    SimpleDateFormat currentDate =    new SimpleDateFormat("dd-MMMM-yyyy");
                     String saveCurrentDate= currentDate.format(calForDate.getTime());

                    Calendar calForTime = Calendar.getInstance();
                    SimpleDateFormat currentTime =    new SimpleDateFormat("HH:mm:ss");
                    String   saveCurrentTime= currentTime.format(calForTime.getTime());

                 Map messageTextBody = new HashMap();
                    messageTextBody.put("messages",messageText);
                    messageTextBody.put("time",saveCurrentTime);
                    messageTextBody.put("date",saveCurrentDate);
                    messageTextBody.put("type","text");
                    messageTextBody.put("from",msgSenderId);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(sender_message_Ref + "/" + message_push_id,messageTextBody);
                    messageBodyDetails.put(receiver_message_Ref+ "/" + message_push_id,messageTextBody);

                    RootRef.child("Messages").updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ChatActivity.this, " Message Sent", Toast.LENGTH_SHORT).show();
                                chatMessageContent.setText("");
                            }
                           else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(ChatActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                chatMessageContent.setText("");
                            }


                        }
                    });

            }
    }


    public void  updateUserStatus( String state){

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate =    new SimpleDateFormat("dd-MM");
        String saveCurrentDate= currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime =    new SimpleDateFormat("HH:mm");
        String  saveCurrentTime= currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UserRef.child(msgSenderId).child("userState").updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {
           ReceiverUserName.setText(msgReceiverName);
           RootRef.child("Users").child(msgReceiverId).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {

                   if (snapshot.exists())
                   {
                       final String profileImage = snapshot.child("profileimage").getValue().toString();
                       final String Usertype = snapshot.child("userState").child("type").getValue().toString();
                       final String lastDate = snapshot.child("userState").child("date").getValue().toString();
                       final String lastTime= snapshot.child("userState").child("time").getValue().toString();
                       Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(ReceiverUserImage);


                       if (Usertype.equals("online"))
                       {
                          UserLastSeen.setText("online");
                       }

                       else
                       {
                           UserLastSeen.setText("last seen" + lastTime+ " "+ lastDate);
                       }

                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });
    }
}