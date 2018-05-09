# StefyUpgrader - Music format Upgrader.

## Program requirements:  
  * ![FFMPEG](https://www.ffmpeg.org/) with  args: --enable-libfdk-aac --enable-nonfree --enable-gpl  
  * ![Youtube-dl](https://github.com/rg3/youtube-dl) 
  
 ![SU](https://i.imgur.com/6J2J70n.png) 
  
## What it can do: 
  * Can convert mp3/m4a/mp4/webm/flac format to ** ![aac](https://en.wikipedia.org/wiki/Advanced_Audio_Coding) ** format.   
        
## Steps for doing that^ :
  - After adding a file the program checks for a youtube code,  
      - if that is the case then tries to download the file with the highest quality  
           then converts that file into aac format.  
      - if the file doesen't have a youtube code, directly converts that file into aac.  
  
## What are the options available :
   * Adding single file/folder.  
   * Select the destination forlder.  
   * Auto delete the downloaded file(if that is the case)  
   * Bitrate (CBR-Constant Bit Rate || VBR - Variable Bit Rate)  
      * If(CBR is selected you can also resample)  
   * Video Download Quality  
![SU Options](https://i.imgur.com/xKmVnRX.png)  
  
## Notes :
  * Developed and tested on Ubuntu only.  
  * Design after 'SoundConverter'
  
