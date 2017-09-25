/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stefy.upgrader.utils;

/**
 *
 * @author spectral369
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StefyUtils {

    public static boolean checkFFPEG_Avconv() {
        String[] command = new String[]{"ffmpeg", "-version"};
        Process proc = null;
        boolean isFFMPEG = false;
        try {
            proc = new ProcessBuilder(command).start();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            isFFMPEG = reader.readLine().contains("version");
            System.out.println("FFMPEG found: " + isFFMPEG);

        } catch (IOException e) {
        }

        return isFFMPEG;
    }

    public static boolean checkYoutube_dl() {
        String[] command = new String[]{"youtube-dl", "--version"};
        Process proc = null;
        boolean isYT = false;
        try {
            proc = new ProcessBuilder(command).start();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            isYT = Integer.parseInt(reader.readLine().substring(0, 4)) >= 2017;
            System.out.println("youtube-dl found: " + isYT);

        } catch (IOException e) {
        }

        return isYT;
    }

}
