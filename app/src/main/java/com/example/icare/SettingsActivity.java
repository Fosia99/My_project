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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userProfileStatus,userName,userFullName,userDob,
            userPhoneNumber,userType,userRelationshipStatus,userCountry,userInterests;
    private CircleImageView userProfileImage;
    private Button updateProfileSettings;
    private RadioButton  gender_female,gender_male;
    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
   private  String currentUserId,Gender;
    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;
    private StorageReference UserProfileImageRef;
    private TextView userGender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUserRef=FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);













        userProfileStatus = (EditText) findViewById(R.id.settings_status);
        userName = (EditText) findViewById(R.id.settings_username);
        userFullName = (EditText) findViewById(R.id.settings_Full_name);
        userDob = (EditText) findViewById(R.id.settings_date_of_birth);
        userPhoneNumber = (EditText) findViewById(R.id.settings_Phone_Number);
        userType = (EditText) findViewById(R.id.settings_UserType);
        userRelationshipStatus = (EditText) findViewById(R.id.settings_Relationship_Status);
        userCountry = (EditText) findViewById(R.id.settings_Country);
        userInterests = (EditText) findViewById(R.id.settings_Interests);
        userProfileImage= (CircleImageView) findViewById(R.id.settings_profile_image);
        updateProfileSettings = (Button) findViewById(R.id.settings_update_button);
        gender_female = (RadioButton) findViewById(R.id.female_gender);
        gender_male = (RadioButton) findViewById(R.id.male_gender);
        loadingBar = new ProgressDialog(this);




      SettingsUserRef.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              if (snapshot.exists()) {

                  String myProImage = snapshot.child("profileimage").getValue().toString();
                  Picasso.get().load(myProImage).placeholder(R.drawable.profile).into(userProfileImage);

                  String myUserName = snapshot.child("username").getValue().toString();
                  userName.setText(myUserName);

                  String myFullName = snapshot.child("fullname").getValue().toString();
                  userFullName.setText(myFullName);

                  String myUserProStatus = snapshot.child("profilestatus").getValue().toString();
                  userProfileStatus.setText(myUserProStatus);

                  String myUserDob = snapshot.child("dateofbirth").getValue().toString();
                  userDob.setText(myUserDob);

                  Gender = snapshot.child("gender").getValue().toString();

                  String myUserPhoneNumber = snapshot.child("phonenumber").getValue().toString();
                  userPhoneNumber.setText(myUserPhoneNumber);

                  String myUserType = snapshot.child("usertype").getValue().toString();
                  userType.setText(myUserType);

                  String myUserRelationshipStatus = snapshot.child("relationshipstatus").getValue().toString();
                  userRelationshipStatus.setText(myUserRelationshipStatus);

                  String myUserCountry = snapshot.child("country").getValue().toString();
                  userCountry.setText(myUserCountry);

                  String myUserInterests = snapshot.child("interest").getValue().toString();
                  userInterests.setText(myUserInterests);

              }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      });


        updateProfileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gender_male.isChecked()) {
                    Gender= gender_male.getText().toString();
                    gender_male.setChecked(true);
                } else if (gender_female.isChecked()) {
                    Gender= gender_female.getText().toString();}
                gender_female.setChecked(true);

                ValidateAccountInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
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
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                android.net.Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");


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

                            Toast.makeText(SettingsActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();
                            final String downloadUrl = String.valueOf(downUri);


                            SettingsUserRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SettingsActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
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

    private void ValidateAccountInfo() {

        String username = userName.getText().toString();
        String fullname = userFullName.getText().toString();
        String gender = Gender;
        String phonenumber= userPhoneNumber.getText().toString();
        String profilestatus= userProfileStatus.getText().toString();
        String dob = userDob.getText().toString();
        String relationshipstatus = userRelationshipStatus.getText().toString();
        String type = userType.getText().toString();
        String country = userCountry.getText().toString();
        String interest =userInterests.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show(); }
       else if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show(); }
       else  if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please enter your gender", Toast.LENGTH_SHORT).show(); }
       else if (TextUtils.isEmpty(phonenumber)) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show(); }
       else  if (TextUtils.isEmpty(profilestatus)) {
            Toast.makeText(this, "Please update your status", Toast.LENGTH_SHORT).show(); }
       else  if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please enter your date of birth", Toast.LENGTH_SHORT).show(); }
       else  if (TextUtils.isEmpty(relationshipstatus)) {
            Toast.makeText(this, "Please choose your relationship status", Toast.LENGTH_SHORT).show(); }
       else if (TextUtils.isEmpty(type)) {
            Toast.makeText(this, "Please choose your user type", Toast.LENGTH_SHORT).show(); }
       else  if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please enter your country", Toast.LENGTH_SHORT).show(); }
        else  if (TextUtils.isEmpty(interest)) {
            Toast.makeText(this, "Please write your interests", Toast.LENGTH_SHORT).show(); }

        else{  loadingBar.setTitle("Account Settings");
            loadingBar.setMessage("Please wait,while we update your account settings");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
                  UpdateAccInfo(username,fullname,gender,phonenumber,profilestatus,dob,relationshipstatus,type,country,interest); }
    }

    private void UpdateAccInfo(String username, String fullname, String gender, String phonenumber, String profilestatus, String dob, String relationshipstatus,
                               String type, String country,String interest) {

        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("fullname", fullname);
        userMap.put("country", country);
        userMap.put("usertype",type);
        userMap.put("gender",gender);
        userMap.put("dateofbirth", dob);
        userMap.put("interest", interest);
        userMap.put("phonenumber", phonenumber);
        userMap.put("relationshipstatus", relationshipstatus);
        userMap.put("profilestatus", profilestatus);

       SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account information successfully updated", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }


            }
        });

    }

    private void SendUserToMainActivity() {

        Intent mainActivityIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }


}

