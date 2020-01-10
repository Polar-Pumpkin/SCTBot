package org.serverct.utils;

import org.serverct.SCTBot;

import java.io.File;

public class BasicUtil {

    public static long getQQ(String target) {
        long qq;
        try {
            qq = Long.parseLong(target);
        } catch(Throwable ignored) {
            qq = SCTBot.CC.getAt(target);
        }
        return qq;
    }

    public static String[] buildParameter(String[] param) {
        String[] result = new String[param.length - 1];
        System.arraycopy(param, 1, result, 0, param.length - 1);
        return result;
    }

    public static void quickDebug(String text) {
        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "[快速Debug] " + text);
    }

    public static File[] getXML(File dataFolder) {
        File[] files = dataFolder.listFiles(file -> file.getName().endsWith(".xml"));
        if(files != null && files.length > 0) {
            return files;
        }
        return null;
    }

}
