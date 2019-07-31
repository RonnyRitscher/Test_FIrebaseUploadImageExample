package com.apps4people.cl_firebaseuploadexample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity {


    private static final String TAG = ImagesActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ImagesAdapter mAdapter;

    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        mProgressBar = findViewById(R.id.pb_images_progressBar);

        mRecyclerView = findViewById(R.id.rv_images);
        mRecyclerView.setHasFixedSize(true);  //Fixiert die größe
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUploads = new ArrayList<>();

        //Welche Storage-Datenbank soll verwendet werden? Beispiel:
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("CS_Firebase_Upload_Example/uploads");


        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long anzBilder = dataSnapshot.getChildrenCount();
                Log.d(TAG, "anzBilder: "+anzBilder);

                if(dataSnapshot.exists()){
                    for(int z=0;z<anzBilder;z++){
                        Log.d(TAG, "z: "+z);


                    }

                    //Inhalte der DB laden:
                    int i =0;
                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){    //FOR-EACH
                        Log.d(TAG, "i: "+i);
                        Upload upload = postSnapshot.getValue(Upload.class);        //TMP UploadObjekt mit dem inhalt der DatabaseRef
                        mUploads.add(upload);                                       //in die Liste speichern
                    }

                    mAdapter = new ImagesAdapter(ImagesActivity.this , mUploads);  //Adapter bestücken

                    mRecyclerView.setAdapter(mAdapter);             //Adapter übergeben
                    mProgressBar.setVisibility(View.INVISIBLE);     //Ausblenden der PB
                }else{
                    Log.d(TAG, "onDataChange: DATASNAPSHOT == NULL");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
