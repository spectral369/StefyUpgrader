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
import com.stefy.upgrader.utils.StefyFormats;
import com.stefy.upgrader.utils.StefyUtils;
import java.io.File;
import java.io.IOException;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.options.AudioEncodingOptions;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

public final class Ffmpeg {

    FFmpeg ffmpeg = null;
    FFprobe ffprobe = null;
    FFmpegProbeResult in = null;
    FFmpegBuilder builder = null;
    FFmpegExecutor executor = null;

    File file = null;
    String outputFormat = null;
    String outputDir = null;
    long bitRate = 192000L;
    int channels = 2;
    int sample_rate = 48_0000;//default
    String modeType = null;

    public Ffmpeg() {

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
    }

    public Ffmpeg(File file, String outputFormat, String outputDir, int bitRate, String type) {
        boolean is = StefyUtils.checkFFPEG_Avconv();
        if (!is) {
            System.out.println("FFMPEG is Missing");
            System.exit(1);
        }

        try {
            this.ffmpeg = new FFmpeg("//usr//bin//ffmpeg");
            this.ffprobe = new FFprobe("//usr//bin//ffprobe");

        } catch (IOException e) {
            System.out.println("ERRR:");
            e.printStackTrace(System.out);
        }

        this.file = file;
        this.outputDir = outputDir;

        this.bitRate = bitRate * 1000L;
        this.modeType = type;
        String s = setOutputFormat(outputFormat);
        if (s != null) {
            this.outputFormat = s;
        } else {
            System.out.println("Wrong format");
            System.exit(1);
        }

    }

    public boolean build() {
        String filename = file.getName().substring(0, file.getName().lastIndexOf("."));

        try {
            in = ffprobe.probe(file.getPath());
        } catch (IOException e) {
            return false;

        }
        if (modeType.contains("VBR")) {
            builder = new FFmpegBuilder()
                    .setInput(in)
                    .overrideOutputFiles(true)
                    .addOutput(outputDir + filename + ".aac")
                    .setAudioChannels(2)
                    .setAudioCodec(outputFormat.toLowerCase())
                    .addExtraArgs("-q:a", "2")
                    .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_48000)//48000
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();
        } else {

            builder = new FFmpegBuilder()
                    .setInput(in)
                    .overrideOutputFiles(true)
                    .addOutput(outputDir + filename + ".aac")
                    .setAudioChannels(2)
                    .setAudioCodec(outputFormat.toLowerCase())
                    .setAudioBitRate(bitRate)
                    .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_48000)//48000
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();

        }
        return true;

    }
    public ProgressListener prog = new ProgressListener() {
        @Override
        public void progress(Progress progress) {
            final double duration_us = in.getFormat().duration * 1000000.0;

            double percentage = progress.out_time_ms / duration_us;

            // Print out interesting information about the progress
            System.out.println(String.format(
                    "[%.0f%%] status:%s  time:%s ms  speed:%.2fx thread:%s",
                    percentage * 100,
                    progress.progress,
                    FFmpegUtils.millisecondsToString(progress.out_time_ms),
                    progress.speed,
                    Thread.currentThread().getName()
            ));
        }
    };

    public void execute() {
        executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder, prog);
        job.run();

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
