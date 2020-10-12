package com.example.icare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputBox;
    private RecyclerView SearchResultList,DisplaySearchResults;

    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchButton = (ImageButton) findViewById(R.id.search_friends_button);
        SearchInputBox = (EditText) findViewById(R.id.search_box_input);

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        SearchResultList.setLayoutManager(linearLayoutManager);


        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputSearchBox = SearchInputBox.getText().toString();
                SearchForFriends(inputSearchBox);
            }
        });

    }

    private void SearchForFriends(String inputSearchBox) {
        Toast.makeText(this, "Searching......", Toast.LENGTH_SHORT).show();

        Query searchFriendsQuery = UsersRef.orderByChild("fullname").startAt(inputSearchBox);

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchFriendsQuery, FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            public FindFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new FindFriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(FindFriendsViewHolder viewHolder, final int position, FindFriends model) {
                final   String clicked_user_id = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setProfileimage(model.getProfileimage());
                viewHolder.setProfilestatus(model.getProfilestatus());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                         Intent personProfile = new Intent(FindFriendsActivity.this,ProfilePersonalActivity.class);
                         personProfile.putExtra("clicked_user_id",clicked_user_id);
                         startActivity(personProfile);
                    }
                });

            }
        }; adapter.startListening();
        SearchResultList.setAdapter(adapter);
    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.all_users_full_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setProfilestatus(String profilestatus) {
            TextView Profilestatus = (TextView) mView.findViewById(R.id.all_users_status);
            Profilestatus.setText(profilestatus);
        }

    }
}