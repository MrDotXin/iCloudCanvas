package com.mrdotxin.icloudcanvas.utils;

public class PathUtils {

    public static String wipeSuffix(String path) {
        int index = path.lastIndexOf('.');
        return path.substring(0, index);
    }
}
