/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stefy.upgrader.downloader;

import com.stefy.upgrader.stefyupgrader.FXMLDocumentController;
import com.stefy.upgrader.utils.StefyUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.io.IOException;
import org.apache.commons.io.FileUtils;

public final class YTDownloader {

    float audioOnlySize = 0;
    int audioOnlyCode = 0;
    String audioOnlyCodeStr = "";
    int audioBestCode = 0;
    String audioBestStr = "";
    Process proc = null;

    private String downloadedFileName = null;
    File f = null;
    String outputDir = null;
    boolean isSel = true;

    String quality = "Standard";
    private String link = "";
    public boolean isValid = false;

    public YTDownloader() {

        if (!StefyUtils.checkYoutube_dl()) {
            System.out.println("Youtube-dl is missing, please isntall: sudo apt install youtube-dl");
            System.exit(2);
        }

    }

    public void setOutputDir(String dir) {
        this.outputDir = dir;
    }

    public YTDownloader(File f, String outputDir, boolean isSelected, String quality) {

        if (!StefyUtils.checkYoutube_dl()) {
            System.out.println("Youtube-dl is missing, please isntall: sudo apt install youtube-dl");
            System.exit(2);
        }
        this.f = f;
        this.outputDir = outputDir;
        this.isSel = isSelected;
        this.quality = quality;
        test();

    }

    /*   
    public File getOrigFile(){
    return this.f;
    }
     */
    public void setQuality(String qual) {
        this.quality = qual;
    }

    public void setDel(boolean isSele) {
        this.isSel = isSele;
    }

    public int getYTFormatCodes(String link) {

        String[] command = new String[]{"youtube-dl", "-F", link};

        try {
            proc = new ProcessBuilder(command).start();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";

            while ((line = reader.readLine()) != null) {
                if (line.contains("m4a")) {
                    audioOnlyCodeStr = line.trim(); 
                }
                if (line.contains("best")) {

                    audioBestStr = line.trim();
                }
            }
            int st = audioOnlyCodeStr.indexOf("m4a");

            String code = audioOnlyCodeStr.substring(0, st).trim();
            audioOnlyCode = Integer.parseInt(code);
            String codeBest = audioBestStr.substring(0, 3).trim();
            

            audioBestCode = Integer.parseInt(codeBest);

            isValid = true;
            return audioBestCode;
        } catch (IOException | NumberFormatException e) {
            isValid = false;
        }
        proc.destroy();
        return 0;

    }

    public boolean downloadYT(String link, int code) {
        try {

            String pa = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "%(title)s-%(id)s.%(ext)s";

            String[] command = new String[]{"youtube-dl", "-f", String.valueOf(code), "-o", pa, link.trim()};

            proc = new ProcessBuilder(command).start();
            String name = null;
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {

                if (line.contains("Destination:")) {

                    downloadedFileName = line.substring(line.indexOf(":") + 1, line.length()).trim();
                     
                    while (proc.isAlive()) {

                        System.out.print(".");
                        Thread.sleep(1000);
                        //  proc.waitFor();
                    }

                    if (!isSel) {
                        File k = new File(downloadedFileName);

                        FileUtils.copyFile(k, new File(outputDir + k.getName()));

                        downloadedFileName = outputDir + k.getName();

                    }
                    proc.destroy();//test
                    return true;
                } else if (line.contains("already")) {
                    System.out.println("already Downloaded");
                    line = line.substring(line.indexOf("]") + 1, line.indexOf("has") - 1).trim();

                    if (isSel) {
                        downloadedFileName = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + name;
                    }

                    while (proc.isAlive()) {
                        //proc.waitFor();
                        System.out.print(".");
                        Thread.sleep(1000);

                    }
                    if (!isSel) {
                        File k = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + line);
                        FileUtils.copyFile(k, new File(outputDir + k.getName()));
                        downloadedFileName = outputDir + k.getName();
                    }
                    proc.destroy();//test
                    return true;
                }

            }

        } catch (IOException | InterruptedException e) {
            e.getMessage();
        }
        proc.destroy();
        return false;
    }

    public String makeYTLink(String code) {
        return "https://www.youtube.com/watch?v=" + code;
    }

    public String getYTCode(File f) {
        int b = f.getName().lastIndexOf(".");
        int a = b - 12;

        if (f.getName().substring(a, b).startsWith("-")) {
            return f.getName().substring(a + 1, b);
        } else {
            return null;
        }
    }

    public String test() {

        String a = getYTCode(f);
        if (a == null) {
            isValid = false;
            downloadedFileName = f.getPath();
            return "withoutCode";

        }

        link = makeYTLink(a);

        try {
            int ytFormatCodes = getYTFormatCodes(link);
               } catch (Exception e) {
                   isValid=false;
            System.out.println("ERROR: Most likely the video is not available anymore !");
             downloadedFileName = f.getPath();
        }

            if (quality.equals("Standard")) {
                downloadYT(link, audioOnlyCode);
            } else if (quality.equals("High")) {
                downloadYT(link, audioBestCode);
            }
   

        if (isSel) {
            FXMLDocumentController.s2.add(downloadedFileName);
        }
        return downloadedFileName;
    }

    public String getDownloadedFileName() {
        return downloadedFileName;
    }

    public String getOutputDir() {
        return outputDir;
    }

}
