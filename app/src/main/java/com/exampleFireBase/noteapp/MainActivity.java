package com.exampleFireBase.noteapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.exampleFireBase.noteapp.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseFirestore firestore;

    private RecyclerView rvNotes;
    private FloatingActionButton btnAdd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Posts");
        firestore  = FirebaseFirestore.getInstance();

        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        btnAdd = findViewById(R.id.btn_add);
        //login("user1@gmail.com", "123456");
        //createNewUser("User2@gmail.com","123456");
        //postDataToRealtimeDB("Hello Dio!");
        //readDataFromRealtimeDB();
        //postDataToFirebaseStore();
//        addPostData(new Post("Viet Pro Vip", "DT5"));
//        addPostData(new Post("Thien Pro Vip", "DT5"));
//        addPostData(new Post("Ni Pro Vip", "DT5"));
//        addPostData(new Post("Huy Pro Vip", "DT5"));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
            }
        });

    }

    public void addNote(){
        AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.add_note, null);
        mDialog.setView(mView);

        AlertDialog dialog = mDialog.create();
        dialog.setCancelable(true);

        EditText edtTitle = mView.findViewById(R.id.edt_edTitle);
        EditText edtContent = mView.findViewById(R.id.edt_edContent);
        Button Save = mView.findViewById(R.id.btn_Save);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = myRef.push().getKey();
                String title = edtTitle.getText().toString();
                String content = edtContent.getText().toString();

                myRef.child(id).setValue(new Post(id, title, content, getRandomColor())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Add Note Successful", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(MainActivity.this, "Add Note Unsuccessful", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(myRef, Post.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Post, PostHolder>(options) {
            @Override
            public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_items, parent, false);
                return new PostHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostHolder holder, int position, Post model) {
                holder.tvTitle.setText(model.getTitle());
                holder.tvContent.setText(model.getContent());
                holder.linearLayout.setBackgroundColor(Color.parseColor(model.getColor()));

                ImageView ivAction = holder.itemView.findViewById(R.id.iv_action);
                ivAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        };

        rvNotes.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_logout:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public TextView tvContent;
        public LinearLayout linearLayout;

        public PostHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvContent = view.findViewById(R.id.tv_content);
            linearLayout = view.findViewById(R.id.layout_note);
        }

    }
    private String getRandomColor(){
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#b2d5ba");
        colors.add("#ff80ed");
        colors.add("#ffa500");
        colors.add("#cb7b97");
        colors.add("#00ffb2");
        colors.add("#e6000d");
        colors.add("#019db1");
        colors.add("#02569b");
        colors.add("#bd9165");
        Random random = new Random();
        return  colors.get(random.nextInt(colors.size()));
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug:", " Login Successful");
                        }else {
                            Log.d("Debug:", " Login Fail");
                        }
                    }
                });
    }

    private void createNewUser (String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug:", " Create New User Successful");
                        }else {
                            Log.d("Debug:", " Create New User Fail");
                        }
                    }
                });
    }

    private void resetPassword(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug:", " reset pass Successful");
                        }else {
                            Log.d("Debug:", " reset pass Fail");
                        }
                    }
                });
    }

    private void postDataToRealtimeDB(String data){
        myRef.setValue(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug:", " post data "+data+" Successful");
                        }else {
                            Log.d("Debug:", " post data "+data+" Fail");
                        }
                    }
                });
    }

    private void readDataFromRealtimeDB(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                Log.d("Debug", "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Debug", "Failed to read value.", error.toException());
            }
        });
    }

    private void postDataToFirebaseStore(){
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first7", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        // Add a new document with a generated ID
        firestore.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Debug", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Debug", "Error adding document", e);
                    }
                });
    }

    private void addPostData(Post postdata){
        DatabaseReference myRefRoot = database.getReference();
        myRefRoot.child("posts").setValue(postdata)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug:", " post data "+postdata+" Successful");
                        }else {
                            Log.d("Debug:", " post data "+postdata+" Fail");
                        }
                    }
                });
    }

}