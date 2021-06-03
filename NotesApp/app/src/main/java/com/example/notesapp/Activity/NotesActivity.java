package com.example.notesapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.Models.firebaseModels;
import com.example.notesapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesActivity extends AppCompatActivity {
    FloatingActionButton mcreatenotesfab;
    FirebaseAuth auth;
    RecyclerView mrecyclerview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<firebaseModels,NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        auth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore  =FirebaseFirestore.getInstance();

        mcreatenotesfab = findViewById(R.id.createnotefab);
        getSupportActionBar().setTitle("All Notes");
        mcreatenotesfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(NotesActivity.this,CreateNotesActivity.class));
            }
        });

        Query query = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("title", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<firebaseModels> allusernotes = new FirestoreRecyclerOptions.Builder<firebaseModels>().setQuery(query, firebaseModels.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<firebaseModels, NoteViewHolder>(allusernotes) {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebaseModels firebaseModels) {

                ImageView popupbtn = noteViewHolder.itemView.findViewById(R.id.menupopbtn);
                     int colourcode = getRandomColor();
                     noteViewHolder.mnote.setBackgroundColor(noteViewHolder.itemView.getResources().getColor(colourcode, null));
                     noteViewHolder.notetitle.setText(firebaseModels.getTitle());
                     noteViewHolder.notecontent.setText(firebaseModels.getContent());

                     String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                     noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             //we have to open note detail activity
                             Intent intent = new Intent(v.getContext(),NotesDetailActivity.class);
                             intent.putExtra("title",firebaseModels.getTitle());
                             intent.putExtra("content",firebaseModels.getContent());
                             intent.putExtra("noteId", docId);
                             v.getContext().startActivity(intent);
                             //Toast.makeText(NotesActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                         }
                     });
                     popupbtn.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             PopupMenu popupMenu = new PopupMenu(v.getContext(),v);
                             popupMenu.setGravity(Gravity.END);
                             popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                 @Override
                                 public boolean onMenuItemClick(MenuItem item) {
                                     Intent intent = new Intent(v.getContext(),EditNoteActivity.class);
                                     intent.putExtra("title",firebaseModels.getTitle());
                                     intent.putExtra("content",firebaseModels.getContent());
                                     intent.putExtra("noteId", docId);
                                     v.getContext().startActivity(intent);

                                     return false;
                                 }
                             });

                             popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                 @Override
                                 public boolean onMenuItemClick(MenuItem item) {
                                     //Toast.makeText(v.getContext(),"Clicked", Toast.LENGTH_SHORT).show();
                                     DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                                     documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {
                                             Toast.makeText(v.getContext(),"This note is deleted.", Toast.LENGTH_SHORT).show();
                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                                         @Override
                                         public void onFailure(@NonNull Exception e) {
                                             Toast.makeText(v.getContext(),"Failed to delete.", Toast.LENGTH_SHORT).show();

                                         }
                                     });
                                     return false;
                                 }
                             });

                             popupMenu.show();
                         }
                     });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };



        mrecyclerview = findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mrecyclerview.setLayoutManager(staggeredGridLayoutManager);
        mrecyclerview.setAdapter(noteAdapter);



    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        private TextView notetitle;
        private TextView notecontent;
        LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notecontent = itemView.findViewById(R.id.notecontent);
            mnote = itemView.findViewById(R.id.note);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                auth.signOut();
                finish();
                startActivity(new Intent(NotesActivity.this,  MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter!=null){
            noteAdapter.stopListening();
        }
    }

    private int getRandomColor(){
        List<Integer> colourcode = new ArrayList<>();
        colourcode.add(R.color.gray);
        colourcode.add(R.color.pink);
        colourcode.add(R.color.color1);
        colourcode.add(R.color.color2);
        colourcode.add(R.color.color3);
        colourcode.add(R.color.color4);
        colourcode.add(R.color.color5);
        colourcode.add(R.color.skyblue);
        colourcode.add(R.color.lightgreen);
        colourcode.add(R.color.green);

        Random random = new Random();
        int number = random.nextInt(colourcode.size());
        return  colourcode.get(number);
    }
}