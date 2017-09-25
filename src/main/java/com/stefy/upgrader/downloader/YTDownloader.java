/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stefy.upgrader.downloader;

import com.stefy.upgrader.utils.StefyUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.io.IOException;
import org.apache.commons.io.FileUtils;

public final class YTDownloader extends Thread {

    float audioOnlySize = 0;
    int audioOnlyCode = 0;
    String audioOnlyCodeStr = "";
    int audioBestCode = 0;
    String audioBestStr = "";
    Process proc = null;

    public static String  downloadedFileName = null;
    File f = null;
    String outputDir = null;
    boolean isSel = true;

    String quality = "Standard";
    private String link = "";
    public volatile static boolean isValid = false;

    public YTDownloader() {

        if (!StefyUtils.checkYoutube_dl()) {
            System.out.println("Youtube-dl is missing, please isntall: sudo apt install youtube-dl");
            System.exit(2);
        }

    }

    public void setFile(File f) {
        this.f = f;
    }

    public void setOutputDir(String dir) {
        this.outputDir = dir;
    }

    public YTDownloader(File f, String outputDir, boolean isSelected,String quality) {

        if (!StefyUtils.checkYoutube_dl()) {
            System.out.println("Youtube-dl is missing, please isntall: sudo apt install youtube-dl");
            System.exit(2);
        }
        this.f = f;
        this.outputDir = outputDir;
        this.isSel = isSelected;
        this.quality = quality;

    }
    
    public void setQuality(String qual){
        this.quality=qual;
    }

    public void setDel(boolean isSele) {
        this.isSel = isSele;
    }

    public void getYTFormatCodes(String link) {

        String[] command = new String[]{"youtube-dl", "-F", link};

        try {
            proc = new ProcessBuilder(command).start();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("m4a")) {
                    if (line.contains("MiB")) {
                        int in = line.indexOf("MiB");
                        float curr = Float.parseFloat(line.substring(in - 4, in));
                        System.out.println("val: " + curr);
                        if (audioOnlySize < curr) {
                            audioOnlySize = curr;
                            audioOnlyCodeStr = line;
                        }
                    }
                } else {
                    if (line.contains("best")) {

                        audioBestStr = line;

                    }
                }
            }
            int st = audioOnlyCodeStr.indexOf("m4a");

            String code = audioOnlyCodeStr.substring(0, st).trim();
            audioOnlyCode = Integer.parseInt(code);
            String codeBest = audioBestStr.substring(0, 3).trim();
            audioBestCode = Integer.parseInt(codeBest);

            isValid = true;
        } catch (IOException | NumberFormatException e) {
            e.getMessage();
            isValid = false;
        }
        proc.destroy();
    }

    public boolean downloadYT(String link, int code) {
        try {
            /*
            File target = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "chromedriver");
				
             outputDir + "%(title)s-%(id)s.%(ext)s"
            
            
             */
            String pa = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "%(title)s-%(id)s.%(ext)s";

            String[] command = new String[]{"youtube-dl", "-f", String.valueOf(code), "-o", pa, link.trim()};

            proc = new ProcessBuilder(command).start();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println("-> " + line);
                if (line.contains("Destination:")) {
              
                    if (isSel) {
                      //  System.out.println("is selected: "+line.substring(line.indexOf(":") + 1, line.length()).trim());
                        downloadedFileName = line.substring(line.indexOf(":") + 1, line.length()).trim();
                    }

                    // downloadedFileName=System.getProperty("java.io.tmpdir") + System.getProperty("file.separator")+ name;
                    System.out.println("download Name: " + downloadedFileName + " issel: " + isSel);
                    System.out.println("working");
                    while (proc.isAlive()) {
                        //proc.waitFor();
                        System.out.print(".");
                        Thread.sleep(1000);

                    }
                    if (!isSel) {
                        File k = new File(pa);
                        FileUtils.copyFile(k, new File(outputDir + k.getName()));
                        downloadedFileName = outputDir + k.getName();
                    }
                    proc.destroy();//test
                    return true;
                } else if (line.contains("already")) {

                    //downloadedFileName = line.substring(line.lastIndexOf("/") + 1, line.indexOf("m4a") + 3).trim();
                   // String name = line.substring(line.lastIndexOf("/") + 1, line.indexOf("mp4") + 3).trim();
                   String name=  line.substring(line.indexOf("]")+1,line.indexOf("has")-1).trim();
                    System.out.println("Line: "+line);
                    if (isSel) {
                        downloadedFileName = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + name;
                    }

                    while (proc.isAlive()) {
                        //proc.waitFor();
                        System.out.print(".");
                        Thread.sleep(1000);
                    }
                    if (!isSel) {
                        File k = new File(pa);
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

    public void setDownloadFormat(String type) {
        //TODO asda

    }

    @Override
    public void run() {
        try {
            String a = getYTCode(f);
            if (a == null) {
                isValid = false;
                interrupt();
            }
            link = makeYTLink(a);

            getYTFormatCodes(link);
             if(quality.equals("Standard"))
            downloadYT(link, audioOnlyCode);
             else if(quality.equals("High"))
                 downloadYT(link, audioBestCode);

        } catch (Exception e) {
            System.out.println("err smg: " + e.getMessage());
        }

    }

    public String getDownloadedFileName() {
        return downloadedFileName;
    }

    public String getOutputDir() {
        return outputDir;
    }

}
