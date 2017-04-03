package com.ismael.botonera.model;

import java.io.File;

/**
 * Created by Ismael on 27/03/2016.
 */
public class FileModel {

    private String fileName;
    private String filePath;

    public FileModel(File file){
        setFileName(file.getName());
        setFilePath(file.getPath());
    }


    public String getFileName() {
        return fileName;
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
