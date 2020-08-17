src=/data/video/
inotifywait -mq --timefmt '%d/%m/%y %H:%M' --format '%T %w%f%e' -e create $src | while read file
do
        file=${file/CREATE/}
        file=${file#*/}
        filetype=${file##*.}
        if [ ${filetype} == 'mp4' ]; then
                tempfilename=${file##*/}
                name=${tempfilename%.*}

                eval "mkdir /data/video/${name} -p"
                eval "ffmpeg -i /data/video/${name}.mp4 -ss 1 -f image2  /data/video/${name}/default.jpeg"
                eval "ffmpeg -i /data/video/${name}.mp4 -metadata rotate=\"\" /data/video/${name}/.temp.mp4"
                eval "ffmpeg -i /data/video/${name}/.temp.mp4 -codec copy -vbsf h264_mp4toannexb -map 0 -f segment -segment_list /data/video/${name}/default.m3u8 -segment_time 2 /data/video/${name}/%03d.ts"
        fi
done
