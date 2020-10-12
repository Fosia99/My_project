package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.firebase.auth.FirebaseAuth;
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

public class GroupClickActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private String currentUserId, GroupKey, databaseUserId;
    private ImageView groupIcon;
    private Button addpost,editGroup,deleteGroup;;
    private TextView GroupDescription,GroupName;
    private DatabaseReference UsersRef, ClickGroupsRef,LikesRef;
    Boolean LikeChecker = false;
    private RecyclerView postsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_click);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        GroupKey = getIntent().getExtras().get("GroupKey").toString();
        ClickGroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(GroupKey);


        groupIcon= (ImageView) findViewById(R.id._click_group_icon);
        GroupName = (TextView) findViewById(R.id.click_group_name);
        GroupDescription = (TextView) findViewById(R.id.click_group_description);

        editGroup = (Button) findViewById(R.id.edit_group);
        deleteGroup = (Button) findViewById(R.id.delete_group);
        addpost = (Button) findViewById(R.id.click_update_new_post_button) ;
        postsList = (RecyclerView) findViewById(R.id.all_posts_list);
        postsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postsList.setLayoutManager(linearLayoutManager);


        deleteGroup.setVisibility(View.INVISIBLE);
       editGroup.setVisibility(View.INVISIBLE);


         addpost.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 SendUserToPostActivity();
             }
         });
        ClickGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    databaseUserId = snapshot.child("uid").getValue().toString();
                    final  String Name= snapshot.child("name").getValue().toString();
                    final  String Description= snapshot.child("description").getValue().toString();

                    GroupName.setText(Name);
                    GroupDescription.setText(Description);

                    String Image = snapshot.child("groupimages").getValue().toString();
                    Picasso.get().load(Image).placeholder(R.drawable.select_image).into(groupIcon);


                    if (currentUserId.equals(databaseUserId)) {
                        deleteGroup.setVisibility(View.VISIBLE);
                        editGroup.setVisibility(View.VISIBLE);
                    }

                    editGroup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(Name);
                            EditCurrentPost(Description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });

        DisplayAllPosts();
    }

    private void SendUserToPostActivity() {

        Intent postActivityIntent = new Intent(GroupClickActivity.this, PostActivity.class);
        startActivity(postActivityIntent);
        finish();

    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupClickActivity.this);
        builder.setTitle("Edit Group Information ");

        final EditText inputField = new EditText(GroupClickActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update Group", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

               ClickGroupsRef.child("description").setValue(inputField.getText().toString());
                ClickGroupsRef.child("name").setValue(inputField.getText().toString());
                Toast.makeText(GroupClickActivity.this, "Group updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog =builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }


    private void DeleteCurrentPost() {
        ClickGroupsRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Group Deleted Successfully", Toast.LENGTH_SHORT).show();
    }
    private void DisplayAllPosts() {

        Query PostsOrder= ClickGroupsRef.child("Posts").orderByChild("count");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(PostsOrder, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_posts_layout, parent, false);

                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostsViewHolder viewHolder, int position, Posts model) {

                final String PostKey = getRef(position).getKey();


                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(model.getProfileimage());
                viewHolder.setPostimage(model.getPostimage());
                viewHolder.setLikesButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ClickPostIntent = new Intent(GroupClickActivity.this, ClickPostActivity.class);
                        ClickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(ClickPostIntent);
                    }
                });


                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent CommentsIntent = new Intent(GroupClickActivity.this, CommentsActivity.class);
                        CommentsIntent.putExtra("PostKey", PostKey);
                        startActivity(CommentsIntent);
                    }
                });


                viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (LikeChecker.equals(true)) {
                                    if (snapshot.child(PostKey).hasChild(currentUserId)) {
                                        LikesRef.child(PostKey).child(currentUserId).removeValue();
                                        LikeChecker = false;
                                    } else {
                                        LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                        LikeChecker = false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
            }
        };
        adapter.startListening();
        postsList.setAdapter(adapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNumberofLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNumberofLikes = (TextView) mView.findViewById(R.id.display_number_of_likes);
        }

        public void setLikesButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(PostKey).hasChild(currentUserId)) {

                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNumberofLikes.setText(Integer.toString(countLikes) + (" likes"));
                    } else {

                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNumberofLikes.setText(Integer.toString(countLikes) + (" likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {


                }
            });
        }

        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_text_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date) {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_text_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(String postimage) {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(PostImage);
        }


    }


    private void SendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(GroupClickActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();

    }

}
