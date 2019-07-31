package com.apps4people.cl_firebaseuploadexample;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button mBtnChooseImage, mBtnUploadImages;
    private TextView mTvShowUploads;
    private EditText mEtImageName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask; //Um doppelte Enträge zur gleichen Zeit zu vermeiden


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnChooseImage = findViewById(R.id.btn_chooseImage);
        mBtnUploadImages = findViewById(R.id.btn_uploadImage);
        mTvShowUploads = findViewById(R.id.tv_showUploads);
        mEtImageName = findViewById(R.id.et_imageName);
        mImageView = findViewById(R.id.iv_image);
        mProgressBar = findViewById(R.id.progressBar);

        mBtnChooseImage.setOnClickListener(this);
        mBtnUploadImages.setOnClickListener(this);
        mTvShowUploads.setOnClickListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("CS_Firebase_Upload_Example/uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("CS_Firebase_Upload_Example/uploads");


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_chooseImage:
                bildAuswaehlen();
                break;


            case R.id.btn_uploadImage:
                if(mUploadTask!=null && mUploadTask.isInProgress()){     //Teste ob bereits ein Task läuft UND ob der aktuelle in bearbeitung ist
                    Toast.makeText(this, "Upload is in Progress...", Toast.LENGTH_SHORT).show();
                }else{
                    bildHochladen();
                }
                break;


            case R.id.tv_showUploads:
                zeigeBilder();
                break;
        }
    }

    private void bildAuswaehlen() {
        Log.d(TAG, "bildAuswaehlen: ");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent , PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");

        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            mImageUri = data.getData();  //erhalte die Uri zu dem Bild

            // Bild anzeigen
            //Picasso.get().load(mImageUri).into(mImageView);   //Über Lib anzeigen lassen, alt: Glide
            Glide.with(getApplication()).load(mImageUri).into(mImageView); //...über Glide
            // mImageView.setImageURI(mImageUri);
        }
    }

    private String erhalteBildDateieendung(Uri uri){
        Log.d(TAG, "erhalteBildDateieendung: ");
        //erhalte die Dateiendung (.jpg) des hochzuladenen Bildes
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    private void bildHochladen() {
        Log.d(TAG, "bildHochladen: ");
        if(mImageUri!=null){

            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + erhalteBildDateieendung(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {  //ExtraTask um doppelte Enträge zur gleichen Zeit zu vermeiden
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: ");
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(0);
                            }
                        }, 500 //Aktualmisierungszeit
                    );

                    Toast.makeText(MainActivity.this, "Upload Succsessful", Toast.LENGTH_SHORT).show();
                    Upload upload = new Upload(mEtImageName.getText().toString().trim() , taskSnapshot.getUploadSessionUri().toString());  //Java Klasse Upload
                    Log.d(TAG, " taskSnapshot.getUploadSessionUri().toString(): "+ taskSnapshot.getUploadSessionUri().toString());
                    Log.d(TAG, "mEtImageName.getText().toString().trim(): "+ mEtImageName.getText().toString().trim());
                    Log.d(TAG, "upload: "+upload);

                    String uploadId = mDatabaseRef.push().getKey(); //Erstellt den Storage-Key
                    Log.d(TAG, "uploadId: "+uploadId);
                    mDatabaseRef.child(uploadId).setValue(upload);  //Erstellt den Storage-Value


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {  //PROGRESS FORTSCHRITT
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() );  //Prozentuale anzeige des UploadFortschritts
                    mProgressBar.setProgress( (int) progress);  //
                }
            });
        }else{
            Toast.makeText(this, "NoFileSelected", Toast.LENGTH_SHORT).show();
        }

    }
    private void zeigeBilder() {
        Log.d(TAG, "zeigeBilder: ");
        Intent intent = new Intent(this , ImagesActivity.class);
        startActivity(intent);
    }




}
