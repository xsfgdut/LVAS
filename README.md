# android-LVAS
一款基于IJKPlayer的直播App，实现了对直播流RTSP、RTMP、HTTP流的录制，流录制的文件格式为MP4、MOV，支持音频单独录制（MP3、WAV），核心功能已经编译成so库，核心代码参考另一个工程：RLIJKPlayer

该工程的so库中对录制的视频的相关参数进行特定设置，可能不太适合所有的流，录制的视频的速率也进行了特殊设置，这个是出于商业目的。本项目有很大的局限性，仅供大家学习，真正需要用到商业项目还请各位重新编译IJKPlayer，有问题可以加QQ或者微信,QQ:942737690,微信：lm3515

工程直播流测试地址：rtmp://live.hkstv.hk.lxdns.com/live/hks
