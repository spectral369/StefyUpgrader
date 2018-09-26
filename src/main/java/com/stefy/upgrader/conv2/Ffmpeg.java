/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stefy.upgrader.conv2;

/**
 *
 * @author spectral369
 */
import com.stefy.upgrader.stefyupgrader.FXMLDocumentController;
import com.stefy.upgrader.utils.StefyFormats;
import com.stefy.upgrader.utils.StefyUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

public final class Ffmpeg {

    FFmpeg ffmpeg = null;
    FFprobe ffprobe = null;
    FFmpegProbeResult in = null;
    FFmpegBuilder builder = null;
    FFmpegExecutor executor = null;

    final File file;
    String outputFormat = null;
    String outputDir = null;
    long bitRate = 192000L;
    int channels = 2;
    int sample_rate = 48_0000;//default
    String modeType = null;
    private boolean compress = false;

    /* public  Ffmpeg() {

        boolean is = StefyUtils.checkFFPEG_Avconv();
        if (!is) {
            System.out.println("FFMPEG is Missing. Please do: sudo apt-get install ffmpeg");
            System.exit(1);
        }

        try {
            this.ffmpeg = new FFmpeg("//usr//bin//ffmpeg");
            this.ffprobe = new FFprobe("//usr//bin//ffprobe");

        } catch (IOException e) {
            System.out.println("ERRR:");
            e.printStackTrace(System.out);
        }
        this.file =null;
    }*/
    public Ffmpeg(File file, String outputFormat, String outputDir, int bitRate, String type, boolean compress) {
        boolean is = StefyUtils.checkFFPEG_Avconv();
        if (!is) {
            System.out.println("FFMPEG is Missing");
            System.exit(1);
        }

        try {
            this.ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
            this.ffprobe = new FFprobe("/usr/local/bin/ffprobe");

        } catch (IOException e) {
            System.out.println("ERRR:");
            e.printStackTrace(System.out);
        }

        this.file = file;
        this.outputDir = outputDir;
        if (bitRate == 0) {
            this.bitRate = 0;
        } else {
            this.bitRate = bitRate * 1000L;
        }

        this.modeType = type;
        this.compress = compress;
        String s = setOutputFormat(outputFormat);
        if (s != null) {
            this.outputFormat = s;
        } else {
            System.out.println("Wrong format");
            System.exit(1);
        }
        if (build()) {
            execute();
        }
    }

    public boolean build() {

        String filename = file.getName().substring(0, file.getName().lastIndexOf("."));

        File o = Paths.get(file.getPath()).toFile();

        try {

            in = ffprobe.probe(o.getAbsolutePath());
        } catch (IOException e) {
            Logger.getLogger(Ffmpeg.class.getName()).log(Level.SEVERE, e.getMessage(), e);

            return false;
        }
        if (modeType.contains("VBR")) {
            if (!new File(outputDir).exists()) {
                new File(outputDir).mkdir();
            }
            List<String> flags = new LinkedList<>();
            flags.add("-c:a");
            flags.add("libfdk_aac");
            flags.add("-vbr");
            flags.add("4");
            if (compress) {
                flags.add("-profile:a");
                flags.add("aac_he_v2");
            }
            flags.add("-map_metadata");
            flags.add("0");
            flags.add("-write_id3v2");
            flags.add("3");
            flags.add("-write_id3v2");
            flags.add("1");
            
            String[] array =new String[flags.size()];
           for(int i = 0;i<flags.size();i++){
               array[i]=flags.get(i);
           }

            builder = new FFmpegBuilder()
                    .setInput(in)
                    .overrideOutputFiles(true)
                    .addOutput(outputDir + filename + "." + outputFormat.toLowerCase())
                    .setAudioChannels(2)
                    // .setAudioQuality(1)
                    .addExtraArgs(array)
                    // .setAudioCodec(outputFormat.toLowerCase())
                    // .setAudioCodec("libfdk_aac")
                   // .addExtraArgs("-c:a", "libfdk_aac")
                   // .addExtraArgs("-vbr", "4")
                    // .addExtraArgs("-profile:a", "aac_he_v2")
                   // .addExtraArgs("-map_metadata", "0")
                    //.addExtraArgs("-write_id3v2", "3")
                   // .addExtraArgs("-write_id3v2", "1")
                    // .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_48000)//48000
                    //.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();
        } else {
          
            if (!new File(outputDir).exists()) {
                new File(outputDir).mkdir();
            }

            if (bitRate == 0) {
                bitRate = in.getFormat().bit_rate;
            }
            List<String> flags = new LinkedList<>();
            if (compress) {
                flags.add("-profile:a");
                flags.add("aac_he_v2");
            }
            flags.add("-map_metadata");
            flags.add("0");
            flags.add("-write_id3v2");
            flags.add("3");
            flags.add("-write_id3v2");
            flags.add("1");
              String[] array =new String[flags.size()];
           for(int i = 0;i<flags.size();i++){
               array[i]=flags.get(i);
           }
            
            
            builder = new FFmpegBuilder()
                    .setInput(in)
                    .overrideOutputFiles(true)
                    .addOutput(outputDir + filename + "." + outputFormat.toLowerCase())
                    .setAudioChannels(2)
                    //   .setAudioQuality(1)
                    .setAudioCodec("libfdk_aac")
                    .setAudioBitRate(bitRate)
                    .addExtraArgs(array)
                 //   .addExtraArgs("-map_metadata", "0")
                  //  .addExtraArgs("-write_id3v2", "3")
                   // .addExtraArgs("-write_id3v2", "1")
                    //.addExtraArgs("-profile:a", "aac_he_v2")
                    //.setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_48000)//48000

                    // .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();

        }
        // builder.setVerbosity(FFmpegBuilder.Verbosity.VERBOSE);
        System.out.println("Build done!");
        return true;

    }
    public ProgressListener prog = new ProgressListener() {

        @Override
        public void progress(Progress progress) {

            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            double percentage = progress.out_time_ns / duration_ns;

            System.out.println(String.format(
                    "[%.0f%%] status:%s time:%s ms speed:%.2fx thread:%s",
                    percentage * 100,
                    progress.status,
                    FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                    progress.speed,
                    Thread.currentThread().getName()
            ));

        }
    };

    public void execute() {
        executor = new FFmpegExecutor(ffmpeg, ffprobe);
        System.out.println("step1 done");
        FFmpegJob job = executor.createJob(builder, prog);
        System.out.println("step2 done");
        job.run();
        System.out.println("step3 done");
        int index = FXMLDocumentController.s2.indexOf(file.getPath());
        //System.out.println("ff delete file: "+file.getPath()+ " "+index+" "+FXMLDocumentController.s2.get(index));
        new File(FXMLDocumentController.s2.remove(index)).delete();

    }

    public void deleteFile() {

        this.file.delete();
    }

    public void setBitRate(int bitrate) {
        this.bitRate = bitrate * 1000L;
    }

    public void setOutputDir(String path) {
        this.outputDir = path;
    }

    public String setOutputFormat(String form) {
        String format = form.trim().toUpperCase();
        for (StefyFormats c : StefyFormats.values()) {
            if (c.name().equals(format)) {
                return format;

            }
        }
        return format;

    }

}
