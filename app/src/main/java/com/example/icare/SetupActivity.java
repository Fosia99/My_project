package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName,dateofbirth,usertype,gender;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    String currentUserID;
    final static int Gallery_Pick = 1;
    private Object Uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");



        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_country);
        dateofbirth = (EditText) findViewById(R.id.setup_DateofBirth);
        usertype = (EditText) findViewById(R.id.setup_usertype);
        gender= (EditText) findViewById(R.id.setup_gender);


        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetUpInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if (snapshot.hasChild("profileimage")) {
                        String image = snapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    } else {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
@Override

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
        android.net.Uri imageUri = data.getData();

        //Crop the Image
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);

        // Get the Cropped Image
    }

    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);

        if (resultCode == RESULT_OK) {

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait,while we update your profile image");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            android.net.Uri resultUri = result.getUri();

            final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");


            filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();

                        Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();
                        final String downloadUrl = String.valueOf(downUri);


                        UsersRef.child("profileimage").setValue(downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivity(selfIntent);



                                            Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }

                                    }

                                });
                    }
                }

            });
        }else {
            Toast.makeText(this, "Error Occurred: Image can not be cropped", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }

    }
}

 private void SaveAccountSetUpInformation () {

        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();
        String dob = dateofbirth.getText().toString();
        String userTyp = usertype.getText().toString();
        String Gender = gender.getText().toString();

     if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please enter your country", Toast.LENGTH_SHORT).show();
        }
     if (TextUtils.isEmpty(dob)) {
         Toast.makeText(this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
     }
     if (TextUtils.isEmpty(userTyp)) {
         Toast.makeText(this, "Please choose your user type", Toast.LENGTH_SHORT).show();
     }
     if (TextUtils.isEmpty(Gender)) {
         Toast.makeText(this, "Please enter your gender", Toast.LENGTH_SHORT).show();
     }
     else {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait,while we are setting up your account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("gender", Gender);
            userMap.put("usertype",userTyp);
            userMap.put("dateofbirth", dob);
            userMap.put("interest", "");
            userMap.put("phonenumber", "");
            userMap.put("relationshipstatus", "");
            userMap.put("profilestatus", "");


            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }


                }
            });

        }


    }

    private void SendUserToMainActivity() {

            Intent mainActivityIntent = new Intent(SetupActivity.this,MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivityIntent);
            finish();
        }

    }
