package com.ismael.botonera.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.ismael.botonera.R;
import com.ismael.botonera.model.FileModel;

import org.jitsi.impl.neomedia.codec.audio.opus.Opus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SelectFile extends AppCompatActivity {



    File root;
    File currentFolder;
    ListView lvFiles;
    ArrayList<FileModel> fileNamesList = new ArrayList<FileModel>();
    Button buttonBack;
    TextView tvCurrentFolder;
    private static final int EXPLORER_DIALOG_KEY = 0;
    EditText etButtonName;
    private static final String[] VALID_EXTENSIONS = {"mp3","aac","m4a","3ga","opus"};
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private int selectedPosition;
    FloatingActionButton fab;
    ImageButton btnRecord;
    ImageButton btnPlay;
    boolean isRecording = false;

    private static int BUFFER_SIZE = 1024 * 1024;
    private ByteBuffer decodeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private long opusState;

    private final int APPLICATION_PERMISSIONS = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get Views
        buttonBack = (Button) findViewById(R.id.btn_up);
        lvFiles = (ListView) findViewById(R.id.file_list);
        tvCurrentFolder = (TextView) findViewById(R.id.txt_selected_file_name);
        etButtonName = (EditText) findViewById(R.id.et_file_name);
        fab = (FloatingActionButton) findViewById(R.id.fab_add_audio);
        btnRecord = (ImageButton) findViewById(R.id.btn_record);

        //Initialize media player
        mediaPlayer = new MediaPlayer();

        //btnPlay = (ImageButton) findViewById(R.id.btn_play);
        //Set action for record button
        btnRecord.setOnClickListener(new CustomRecordButton());


        //Setting action for back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFiles(currentFolder.getParentFile());
            }
        });

        //Setting action for listView
        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get the selected file
                File selectedFile = new File(fileNamesList.get(position).getFilePath());

                //If the file is directory list the files
                if(selectedFile.isDirectory()){
                    listFiles(selectedFile);
                }else{
                    String fileName = selectedFile.getName();
                    //Split to get the name and the extension
                    String[] nameArray = fileName.split("\\.");
                    if(nameArray.length == 2){
                        String extension = nameArray[1];
                        if(isValidFile(extension)){

                            try {
                                fab.setEnabled(true);
                                selectedPosition = position;
                                etButtonName.setText(nameArray[0]);
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(selectedFile.getAbsolutePath());

                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(SelectFile.this,e.toString(),Toast.LENGTH_LONG).show();
                            }
                        }else{
                            //La extension no es valida
                            Toast.makeText(SelectFile.this,getText(R.string.not_valid_extension),Toast.LENGTH_LONG).show();
                        }
                    }else{
                        //El archivo no tiene extension
                        Toast.makeText(SelectFile.this,getText(R.string.not_valid_extension),Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        //Setting action for floatin button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveFile();

            }
        });
    }

    private void saveFile() {
        try {
            //Get the selected file
            File selectedFile = new File(fileNamesList.get(selectedPosition).getFilePath());

            //Get file extension
            String completeName = selectedFile.getName();
            String[] splittedName = completeName.split("\\.");
            String extension = "";
            if(splittedName.length > 1){
                extension = splittedName[splittedName.length -1 ];
            }

            //Create a file to save
            File fileToSave = new File(getExternalFilesDir(null),etButtonName.getText().toString() + "." + extension);

            //Write into the new file
            InputStream inputStream = new FileInputStream(selectedFile);
            byte[] buffer = new byte[1024];
            int len;
            FileOutputStream outputStream = new FileOutputStream(fileToSave);
            while((len = inputStream.read(buffer)) > 0){
                outputStream.write(buffer,0,len);
            }

            //Close all streams
            inputStream.close();
            outputStream.close();

            Toast.makeText(SelectFile.this, getText(R.string.button_added_success), Toast.LENGTH_LONG).show();
            finish();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(SelectFile.this,e.toString(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(SelectFile.this,e.toString(),Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //Initialize root and current folder
        initialize_file_list();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }



    private void initialize_file_list() {

        int hasPermissionRead = ContextCompat.checkSelfPermission(SelectFile.this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if(hasPermissionRead == PackageManager.PERMISSION_GRANTED) {
            root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            currentFolder = root;
            //Initialize the list
            listFiles(root);
        }
        else{
            ActivityCompat.requestPermissions(SelectFile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, APPLICATION_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean hasPermission = true;
        switch(requestCode){
            case APPLICATION_PERMISSIONS:
                if(grantResults.length > 0){
                    int i = 0;
                    while (hasPermission && i < grantResults.length){
                        if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                            hasPermission = false;
                        }
                        i++;
                    }
                }
                if(hasPermission)
                    initialize_file_list();
        }
    }


    public boolean isValidFile(String extension){
        boolean isValid = false;
        for(String validExtension: VALID_EXTENSIONS){
            if(validExtension.equals(extension)){
                isValid = true;
            }
        }
        return isValid;
    }



    public void listFiles(File f){
        fileNamesList.clear();
        fab.setEnabled(false);
        if(f != null){
            //If root disable back button
            if(f.equals(root)){
                buttonBack.setEnabled(false);
            }else{
                buttonBack.setEnabled(true);
            }

            //Update current folder
            currentFolder = f;
            tvCurrentFolder.setText(f.getPath());

            //Get directory files
            File[] files = f.listFiles();

            //Load the directories in the array list

            if(files != null) {
                for (File index : files) {

                    fileNamesList.add(new FileModel(index));
                }
            }

        }
        //Create and set the adapter
        CustomFileListAdapter customFileListAdapter = new CustomFileListAdapter();
        lvFiles.setAdapter(customFileListAdapter);
        //Disable add button




    }

    protected class CustomFileListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return fileNamesList.size();
        }

        @Override
        public Object getItem(int position) {
            return fileNamesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.file_list_item,parent,false);
            TextView txtFileName = (TextView) convertView.findViewById(R.id.txt_list_file_name);
            txtFileName.setText(fileNamesList.get(position).getFileName());
            return convertView;
        }
    }


    protected class CustomRecordButton implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String recordName = etButtonName.getText().toString() + ".mp3";
            if(recordName.equals("") && !isRecording){
                Toast.makeText(SelectFile.this,getText(R.string.record_name_error),Toast.LENGTH_LONG).show();
            }else{

                //Change button state
                btnRecord.setActivated(!btnRecord.isActivated());

                //If is not recording starts recording
                if(!isRecording){
                    //Stop media player
                    mediaPlayer.reset();
                    //New instance of media recorder
                    mediaRecorder = new MediaRecorder();
                    //Prepare recording
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    //Start recording
                    try {
                        String recordPath = getExternalFilesDir(null) + File.separator +  recordName;
                        mediaRecorder.setOutputFile(recordPath);
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(SelectFile.this,e.toString(),Toast.LENGTH_LONG).show();
                    }

                }else{
                    //If is recording stops recording
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    Toast.makeText(SelectFile.this,getText(R.string.button_added_success),Toast.LENGTH_LONG).show();
                    finish();
                }
                isRecording = !isRecording;
            }


        }
    }


}
