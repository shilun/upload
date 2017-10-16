package com.upload.service.utils;

import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by feiFan.gou on 2017/8/17 16:49.
 */
public class CommonKit {

    public static final String empty = "";

    public static boolean isNotEmpty(String string) {

        return null != string && !Objects.equals(string.trim(), empty);
    }

    public static boolean isEmpty(String string) {

        return !isNotEmpty(string);
    }

    public static String trim(String string) {

        if (isEmpty(string)) {
            return empty;
        }
        return string.trim();
    }

    public static boolean fileExist(String path) {

        return new File(path).exists();
    }

    public static boolean checkTime(String time) {

        return Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$").matcher(time).matches();
    }

    public static boolean checkNumber(String number) {

        return Pattern.compile("^\\d+$").matcher(number).matches();
    }


    public static boolean checkParam(String... params) {

        if (null != params) {
            for (String param : params) {
                if (isEmpty(param)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}