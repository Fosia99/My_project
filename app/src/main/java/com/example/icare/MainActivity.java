package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    // Declaring variables

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView groupsList;
    private Toolbar mToolbar;
    private TextView groupName,groupDescription;
    private  ImageView groupIcon;
    private ImageButton AddNewFeature;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, LikesRef,GroupsRef;
    private CircleImageView navProfileImage, profileImage;
    private TextView navProfileUserName;
    String currentUserId;
    private ImageView mygroupIcon;
    Boolean LikeChecker = false;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ICare");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        groupIcon= (ImageView) findViewById(R.id.group_icon);
        groupDescription = (TextView) findViewById(R.id.group_description);
        groupName =  (TextView) findViewById(R.id.group_name);


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        AddNewFeature= (ImageButton) findViewById(R.id.add_feature);
        mygroupIcon = (ImageView) findViewById(R.id.group_icon);

        groupsList = (RecyclerView) findViewById(R.id.all_groups_list);
        groupsList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        groupsList.setLayoutManager(linearLayoutManager);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // View headerView = navigationView.getHeaderView(0);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);



        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("fullname")) {
                        String fullname = snapshot.child("fullname").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }
                    if (snapshot.hasChild("profileimage")) {
                        String image = snapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);


                    } else {
                        Toast.makeText(MainActivity.this, "Profile do not exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override

            public void onCancelled(DatabaseError error) {

            }

        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);
                return false;
            }
        });

        DisplayAllGroups();

        AddNewFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToGroupsActivity();
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    SendUserToSettingsActivity();
                    return true;
                case R.id.navigation_friends:
                    SendUserToFriendsActivity();
                    return true;
                case R.id.navigation_groups:
                    SendUserToGroupsActivity();
                    return true;
                case R.id.navigation_addPost:
                    SendUserToPostActivity();
                    return true;
            }
            return false;
        }
    };

    private void SendUserToGroupsActivity() {
        Intent GroupIntent = new Intent(MainActivity.this, GroupsActivity.class);
        startActivity(GroupIntent);
    }



    public void updateUserStatus(String state) {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        String saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        UsersRef.child(currentUserId).child("userState").updateChildren(currentStateMap);

    }

    private void SendUserToPostActivity() {
        Intent PostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(PostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(current_user_id)) {
                    SendUserToSetupActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_about_cancer:
                SendUserToAboutActivity();
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                SendUserToChatActivity();
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();

                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_schedule:
                Toast.makeText(this, "Schedule", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                SendUserToFriendsActivity();
                break;
            case R.id.nav_contact_us:
                Toast.makeText(this, "Contact Us", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_log_out:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;


        }
    }




    private void DisplayAllGroups() {
        Query orderGroupsInOrder = GroupsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Groups> options =
                new FirebaseRecyclerOptions.Builder<Groups>()
                        .setQuery(orderGroupsInOrder, Groups.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Groups, GroupsViewHolder>(options) {
            @Override
            public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_groups_layout, parent, false);

                return new GroupsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(GroupsViewHolder viewHolder, int position, Groups model) {

                final String GroupKey = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setGroupIcon(model.getGroupIcon());
                viewHolder.setDescription(model.getDescription());

               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ClickPostIntent = new Intent(MainActivity.this, GroupClickActivity.class);
                        ClickPostIntent.putExtra("GroupKey", GroupKey);
                        startActivity(ClickPostIntent);
                    }
                });


            }
        };
        adapter.startListening();
        groupsList.setAdapter(adapter);
        updateUserStatus("online");
    }


    public static class GroupsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public GroupsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;


        }
        public void setGroupIcon(String groupIcon) {
            ImageView image = (ImageView) mView.findViewById(R.id.display_groupIcon);
            Picasso.get().load(groupIcon).into(image);

        }


        public void setDescription(String description) {
            TextView GroupDescription = (TextView) mView.findViewById(R.id.display_groupDescription);
            GroupDescription.setText(description);
        }


        public void setName(String name) {
            TextView GroupName = (TextView) mView.findViewById(R.id.display_groupName);
            GroupName.setText(name);
        }
    }


    private void SendUserToChatActivity() {
        Intent ChatIntent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(ChatIntent);
    }

    private void SendUserToFriendsActivity() {
        Intent FriendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(FriendsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void SendUserToProfileActivity() {
        Intent ProfileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(ProfileIntent);
    }

    private void SendUserToSettingsActivity() {

        Intent SettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingsIntent);

    }

    private void SendUserToSetupActivity() {
        Intent setUpIntent = new Intent(MainActivity.this, SetupActivity.class);
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();
    }

    private void SendUserToAboutActivity() {
        Intent AboutIntent = new Intent(MainActivity.this, AboutCancerActivity.class);
        startActivity(AboutIntent);
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }
}





