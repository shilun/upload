src=/data/img/
inotifywait -mq --timefmt '%d/%m/%y %H:%M' --format '%T %w%f%e' -e create $src | while read file
do
        file=${file/CREATE/}
        file=${file#*/}
        echo "$file"
        filetype=${file##*.}
        if [[ $file == *ISDIR ]]; then
                echo "dir"
        else
                echo "$file"
                tempfilename=${file##*/}
                name=${tempfilename%.*}
                eval "mkdir /data/img/${name} "
                echo "convert -resize 50x50  /data/img/${name}.${filetype}  /data/img/${name}/${name}_50x50.${filetype}"
                eval "convert -resize 50x50  /data/img/${name}.${filetype}  /data/img/${name}/${name}_50x50.${filetype}"
                eval "convert -resize 100x100  /data/img/${name}.${filetype}  /data/img/${name}/${name}_100x100.${filetype}"
                eval "convert -resize 400x400  /data/img/${name}.${filetype}  /data/img/${name}/${name}_400x400.${filetype}"
        fi
done
