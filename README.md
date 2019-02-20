# StefyUpgrader - Music format Upgrader.

## Program requirements:  
  * sudo apt-get install libfdk-aac-dev libopus-dev libopus0 opus-tools yasm build-essential(last 2 are for ffmpeg build)
  * ![FFMPEG](https://www.ffmpeg.org/) build like this: ./configure --enable-libopus --enable-libfdk-aac --enable-nonfree --enable-gpl
  * ![Youtube-dl](https://github.com/rg3/youtube-dl) 
  
 ![SU](https://i.imgur.com/6J2J70n.png) 
  
## What it can do: 
  * Can convert mp3/m4a/mp4/webm/flac format to ** ![aac](https://en.wikipedia.org/wiki/Advanced_Audio_Coding) ** ** ![M4A](https://en.wikipedia.org/wiki/MPEG-4_Part_14#.MP4_versus_.M4A) ** ![opus](https://en.wikipedia.org/wiki/Opus_(audio_format)) ** format.   
        
## Steps for doing that^ :
  - After adding a file the program checks for a youtube code,  
      - if that is the case then tries to download the file with the highest quality  
           then converts that file into selected format.  
      - if the file doesen't have a youtube code, directly converts that file into selected format.  
  
## What are the options available :
   * Adding single file/folder.  
   * Select the destination folder.  
   * Download and convert from YT Link.
   * Auto delete the downloaded file(if that is the case)  
   * Bitrate (CBR-Constant Bit Rate || VBR - Variable Bit Rate)  
      * If(CBR is selected you can also resample)  
   * Video Download Quality  
![SU Options](https://i.imgur.com/xKmVnRX.png)  
  
## Notes :
  * Developed and tested on Ubuntu only.  
  * Design after 'SoundConverter'
  
