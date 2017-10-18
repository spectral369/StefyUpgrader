/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stefy.upgrader.folder;

/**
 *
 * @author spectral369
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Folder {

    String pathToClips = null;
    int amount = 0;
    final String format = ".MP3";
    List<File> files = new ArrayList<>();

    public List<File> getAllFiles() {
        Collections.sort(files);
        return files;
    }


    /*	*/
    public void setAddFiles() {

        files.addAll(Arrays.asList(findFiles(getPath())));

    }

    public Folder(String path) {
        this.pathToClips = path;
        if (pathExists(path)) {
            setAddFiles();
        }

    }

    public void setPath(String path) {
        this.pathToClips = path;
    }

    public Folder() {
    }

    public String getPath() {
        return this.pathToClips;
    }

    public boolean pathExists(String path) {
        File f = new File(path);
        return f.exists() & f.isDirectory();
    }

    public File[] findFiles(String path) {
        File dir = new File(path);
        return dir.listFiles((File dir1, String name) -> name.endsWith(format) || name.endsWith(format.toLowerCase())
        );
    }

    public void addFile(String path) {
        files.add(new File(path));
    }

    public void addFile(File f) {
        files.add(f);
    }

}
