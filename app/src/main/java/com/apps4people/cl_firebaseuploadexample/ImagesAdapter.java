package com.apps4people.cl_firebaseuploadexample;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.util.List;

//4. "extends RecyclerView.Adapter" um Adapter erweitern ->  RecyclerView<ImagesAdapter.ImageViewHolder> + Methoden implementieren
public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private static final String TAG = ImagesAdapter.class.getSimpleName();

    //5. Felder hinzufügen
    private Context mContext;
    private List<Upload> mUploads;

    //6. Construktor mit Feldern
    public ImagesAdapter(Context context , List<Upload> uploads){
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        Log.d(TAG, "onCreateViewHolder: ");
        //7. erstellen der View und des LayoutInflators
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image, viewGroup, false );

        //8. die View zurück geben
        return new ImageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        //9. Namen und Image Anzeigen lassen
        Upload uploadAktuell = mUploads.get(position);
        imageViewHolder.tv_images_name.setText(uploadAktuell.getmName());

        Glide.with(mContext)
                .load(uploadAktuell.getmImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fitCenter()
                .centerCrop()
                .into(imageViewHolder.iv_images_image);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        //7. ändern wichtig - Angabe der List-Size()
        return mUploads.size();
    }

    //1. ImageViewHolder als innereKlasse erstellen
    public class ImageViewHolder extends RecyclerView.ViewHolder{

        //2. Felder für die Anzeige erstellen
        public TextView tv_images_name;
        public ImageView iv_images_image;


        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ImageViewHolder: ");

            //3. die IOnitialmisierung der Felder in der innerClass
            tv_images_name = itemView.findViewById(R.id.tv_images_name);
            iv_images_image = itemView.findViewById(R.id.iv_images_image);
        }
    }
}
