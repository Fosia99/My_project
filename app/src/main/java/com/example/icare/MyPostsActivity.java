package com.example.icare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, UsersRef, LikesRef;
    private String currentUserId;
    private int countLikes;
    Boolean LikeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.myPosts_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostList = (RecyclerView) findViewById(R.id.myPosts_list);
       myPostList .setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");



        DisplayAllMyPosts();
    }

    private void DisplayAllMyPosts() {

        Query myPostsOrder= PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId);

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostsOrder, Posts.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {
            @Override
            public MyPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_posts_layout, parent, false);

                return new MyPostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MyPostsViewHolder viewHolder, int position, Posts model) {

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
                        Intent ClickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                        ClickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(ClickPostIntent);
                    }
                });


                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent CommentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
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
        myPostList.setAdapter(adapter);

    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNumberofLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public MyPostsViewHolder(View itemView) {
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
}