package com.ismael.botonera.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;

import com.ismael.botonera.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShareActivity extends AppCompatActivity implements MediaController.MediaPlayerControl, MediaPlayer.OnCompletionListener{


    MediaController mediaController;
    MediaPlayer mediaPlayer;
    EditText etFileName;
    Uri filePathUri;
    String filePath;
    ParcelFileDescriptor parcelFileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize views
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaController = (MediaController) findViewById(R.id.mediaController);
        etFileName = (EditText) findViewById(R.id.et_file_name);

        //Get intent and handle it
        Intent intent = getIntent();
        Uri intentData = intent.getData();
        if(intent.getType().indexOf("audio/") != -1){
            handleAudioIntent(intent);
        }


        //Add acction to buttons
        FloatingActionButton btnPlay = (FloatingActionButton) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        FloatingActionButton btnStop = (FloatingActionButton) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
            }
        });

        FloatingActionButton btnSave = (FloatingActionButton) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile();
            }
        });


    }

    private void saveFile() {
        try {

            if(filePathUri != null){

                File fileToSave = new File(getExternalFilesDir(null),etFileName.getText().toString() + "." + getFileExtension(filePathUri));
                //Open inputStream form ContentResolver
                InputStream inputStream = getContentResolver().openInputStream(filePathUri);
                //Write into the new file
                byte[] buffer = new byte[1024];
                int len;
                FileOutputStream outputStream = new FileOutputStream(fileToSave);
                while((len = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer,0,len);
                }

                //Close all streams
                inputStream.close();
                outputStream.close();

                //Show message and finish activity
                Toast.makeText(ShareActivity.this, getText(R.string.button_added_success), Toast.LENGTH_LONG).show();
                finish();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(ShareActivity.this,e.toString(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ShareActivity.this,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    private String getFileExtension(Uri fileUri){
        String fileRelativePath = filePath.toString();
        String[] splitPath = fileRelativePath.split("\\.");
        if(splitPath != null && splitPath.length > 1){
            return splitPath[splitPath.length - 1];
        }else{
            return "";
        }
    }

    //Handles audio being shared by another app
    private void handleAudioIntent(Intent intent){
        try{
            filePathUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            parcelFileDescriptor = getContentResolver().openFileDescriptor(filePathUri,"rw");
            FileDescriptor sharedFileDescriptor = parcelFileDescriptor.getFileDescriptor();

            Toast.makeText(ShareActivity.this,filePath,Toast.LENGTH_LONG).show();
            mediaPlayer.reset();
            //Replacing strange %20 characters for spaces
            filePath = filePathUri.toString().replace("%20"," ");
            Log.d("BOTONERA", filePath);
            mediaPlayer.setDataSource(sharedFileDescriptor);
            File sourceFile = new File(filePath);
            etFileName.setText(getFileName(sourceFile.getName()));
            mediaController.setMediaPlayer(this);

        }catch (Exception ex){
            Log.e("ERROR", ex.getMessage(), ex);
        }
    }


    private String getFileName(String completeName){
        try{
            Log.d("BOTONERA",completeName);
            String[] splittedName = completeName.split("\\.");
            if(splittedName.length == 2){
                return splittedName[0];
            }else{
                return completeName;
            }
        }catch (Exception ex){
            Log.e("BOTONERA",ex.getMessage(),ex);
            return "NotValidFile";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    public void start() {
        try {
            mediaPlayer.prepare();
            mediaPlayer.start();

        }catch (Exception ex){
            Log.e("BOTONERA",ex.getMessage(),ex);
        }

    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 100;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.seekTo(0);
    }
}
