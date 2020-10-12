package com.example.icare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ImageButton groupIcon;
    private EditText groupName,groupDescription;
    private StorageReference GroupImagesReference;
    private Button createGroup;
    private DatabaseReference UsersRef, GroupRef;
    private ProgressDialog loadingBar;
    private Uri ImageUri;
    private String saveCurrentTime, saveCurrentDate, postRandomName,downloadUrl,Description,Name;
    private static final int Gallery_Pick =1;
    private long countPosts =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        mAuth= FirebaseAuth.getInstance();
        currentUserId= mAuth.getCurrentUser().getUid();

        GroupImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        groupIcon= (ImageButton) findViewById(R.id.group_icon);
        groupDescription = (EditText) findViewById(R.id.group_description);
        groupName =  (EditText) findViewById(R.id.group_name);

        createGroup = (Button) findViewById(R.id.create_group);

        loadingBar = new ProgressDialog(this);


        mToolbar = (Toolbar) findViewById(R.id.group_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");




        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatePostInformation();
            }
        });

    }

    private void ValidatePostInformation() {
        Description = groupDescription.getText().toString();
        Name = groupName.getText().toString();

        if (ImageUri == null)
        {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }

        else  if (TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please enter a description ", Toast.LENGTH_SHORT).show();
        }

        else{
            loadingBar.setTitle("Creating Group");
            loadingBar.setMessage("Please wait, while we create your new group...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);



            StoreImageToFirebaseStorage();
        }
    }

    private void StoreImageToFirebaseStorage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate =    new SimpleDateFormat("dd-MMMM-yyyy");
      saveCurrentDate= currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime =    new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime= currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath =GroupImagesReference.child("Group Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if(task.isSuccessful())
                {         Uri downUri = task.getResult();
                    downloadUrl = downUri.toString();
                    Toast.makeText(GroupsActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();


                    SavingPostInformationToDatabase();

                }

                else

                {   String message = task.getException().getMessage();
                    Toast.makeText(GroupsActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();

                }
            }
        });



    }

    private void SavingPostInformationToDatabase() {

        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    countPosts =snapshot.getChildrenCount();

                }
                else {
                    countPosts = 0;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {



                    HashMap postsMap = new HashMap();

                    postsMap.put("uid", currentUserId);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("name", Name);
                    postsMap.put("description", Description);
                    postsMap.put("groupimages", downloadUrl);
                    postsMap.put("counter",countPosts);

                    GroupRef.child(Name + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {

                                SendUserToMainActivity();
                                Toast.makeText(GroupsActivity.this, "New group is updated successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                            else {
                                String message = task.getException().getMessage();
                                Toast.makeText(GroupsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }


                        }
                    });

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }






    private void OpenGallery() {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {

            ImageUri = data.getData();
            groupIcon.setImageURI(ImageUri);
        }

    }


    @Override
    public boolean  onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(GroupsActivity.this,MainActivity.class);
        // mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        //finish();
    }


}