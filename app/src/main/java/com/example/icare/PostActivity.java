package com.example.icare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private  TextView AddImage,ProfileName;
    private ImageButton SelectPostImage;
    private static final int Gallery_Pick =1;
    private Uri ImageUri;
    private StorageReference PostsImagesReference;
    private DatabaseReference UsersRef , PostsRef;
    private FirebaseAuth mAuth;
    private String Description;
    private String saveCurrentTime, saveCurrentDate, postRandomName,downloadUrl,current_user_id,GroupKey;
    private ProgressDialog loadingBar;
    private long countPosts =0;
    private CircleImageView profileImage;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth= FirebaseAuth.getInstance();
        current_user_id= mAuth.getCurrentUser().getUid();

        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");



        PostsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

       SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
       PostDescription = (EditText) findViewById(R.id.post_description);
       AddImage= (TextView) findViewById(R.id.add_image_text);
       profileImage =(CircleImageView) findViewById(R.id.profileImage);
       ProfileName = (TextView)  findViewById(R.id.profileName);

       UpdatePostButton = (Button) findViewById(R.id.update_new_post_button);
        loadingBar = new ProgressDialog(this);


        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Post");

        SelectPostImage.setVisibility(View.INVISIBLE);


        AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPostImage.setVisibility(View.VISIBLE);
            }
        });


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

      UpdatePostButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              ValidatePostInformation();
          }
      });

    }

    private void ValidatePostInformation() {

        Description = PostDescription.getText().toString();

        if (ImageUri == null)
        {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }

    else  if (TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please enter a description ", Toast.LENGTH_SHORT).show();
        }

        else{
            loadingBar.setTitle("Add New Post");
        loadingBar.setMessage("Please wait, while we are updating your new post...");
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


    final   StorageReference filePath =PostsImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

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
                    Toast.makeText(PostActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();


                    SavingPostInformationToDatabase();

                }

                else

                    {   String message = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();

                }
            }
        });



    }

    private void SavingPostInformationToDatabase() {

        PostsRef.addValueEventListener(new ValueEventListener() {
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

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String userFullName = snapshot.child("fullname").getValue().toString();
                    String userProfileImage = snapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile_icon).into(profileImage);
                    ProfileName.setText(userFullName);


                    HashMap postsMap = new HashMap();

                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    postsMap.put("counter",countPosts);

                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {

                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Post is updated successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                            else {
                                String message = task.getException().getMessage();
                                Toast.makeText(PostActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
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
            SelectPostImage.setImageURI(ImageUri);
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
        Intent mainActivityIntent = new Intent(PostActivity.this,MainActivity.class);
       // mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        //finish();
    }

}