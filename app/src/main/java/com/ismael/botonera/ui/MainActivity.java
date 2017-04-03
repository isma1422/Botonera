package com.ismael.botonera.ui;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ismael.botonera.R;
import com.ismael.botonera.model.FileModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    GridView gridButtons;
    private ArrayList<FileModel> sounds = new ArrayList<FileModel>();
    private MediaPlayer mediaPlayer;
    private static final int SELECTED_BUTTON_POSITION = 0;
    boolean isDeleteMode = false;
    ImageButton btnBorrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize Media Player
        mediaPlayer = new MediaPlayer();

        //Get views
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        gridButtons = (GridView) findViewById(R.id.grid_layout);
        btnBorrar = (ImageButton) findViewById(R.id.btn_borrar);


        //Set action for delete button
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeleteMode = !isDeleteMode;
                btnBorrar.setActivated(!btnBorrar.isActivated());
            }
        });

        //Set the grid adapter
        gridButtons.setAdapter(new CustomGridAdapter());

        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent selectFileIntent = new Intent(MainActivity.this,SelectFile.class);
                    startActivity(selectFileIntent);
                }
            });
        }else{
            Toast.makeText(MainActivity.this,"Floating action button is null",Toast.LENGTH_LONG).show();
        }
    }




    @Override
    protected void onResume() {
        super.onResume();

        //GetSounds
        getSounds();
        CustomGridAdapter adapter = (CustomGridAdapter)gridButtons.getAdapter();
        adapter.notifyDataSetChanged();

    }

    public void getSounds(){
        //Get the sound files
        File appDirectory = new File(getExternalFilesDir(null).getAbsolutePath());

        if(appDirectory != null){
            File[] soundFiles = appDirectory.listFiles();
            if(soundFiles != null){
                //Add it to the list of sounds
                sounds.clear();
                for(File f: soundFiles){
                    sounds.add(new FileModel(f));
                }
            }

        }


    }

    protected class LongPressButtonClickListener implements View.OnLongClickListener{
        @Override
        public boolean onLongClick(View v) {

            //Get selected sound position
            int position = (Integer) v.getTag();

            //Get file
            File fileToShare = new File(sounds.get(position).getFilePath());

            //Share the sound
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToShare));
            shareIntent.setType("audio/*");
            startActivity(Intent.createChooser(shareIntent,"Compartir"));

            return true;
        }
    }


    protected class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            //Get the position of the button
            int position = (Integer) v.getTag();
            String soundPath = sounds.get(position).getFilePath();
            //If delete mode deletes the button
            if(isDeleteMode){
                //Delete the file and reload the grid view
                File fileToDelete = new File(soundPath);
                fileToDelete.delete();
                getSounds();
                ((CustomGridAdapter)gridButtons.getAdapter()).notifyDataSetChanged();
            }else{
                //If not delete mode reproduce the sound
                try {
                    reproduce(soundPath);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();
                }
                //Algo
            }

        }
    }

    private void reproduce(String soundPath) throws IOException {

        String extension = "";
        String[] splittedPath = soundPath.split("\\.");
        if(splittedPath.length > 1){
            extension = splittedPath[splittedPath.length - 1];
        }
        try{
            //Reproduce the selected sound
            mediaPlayer.reset();
            mediaPlayer.setDataSource(soundPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(Exception ex){
            if(extension.equals("opus")){
                Toast.makeText(MainActivity.this,"Cannot reproduce opus file yet",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this,extension,Toast.LENGTH_SHORT).show();
                Log.e("BOTONERA",ex.getMessage(),ex);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    protected class CustomGridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return sounds.size();
        }

        @Override
        public Object getItem(int position) {
            return sounds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.button_layout,parent,false);

            Button boton = (Button) convertView.findViewById(R.id.img_button);
            TextView nombreBoton = (TextView) convertView.findViewById(R.id.txt_button);

            //Set the button name
            String completeName = sounds.get(position).getFileName();
            String[] splittedName = completeName.split("\\.");
            String soundName = "";
            if(splittedName.length > 1){
                soundName = splittedName[0];
            }else{
                soundName = completeName;
            }
            nombreBoton.setText(soundName);

            //Set tag to the button
            boton.setTag(position);
            boton.setOnClickListener(new ButtonClickListener());
            boton.setOnLongClickListener(new LongPressButtonClickListener());

            return convertView;
        }
    }


}
