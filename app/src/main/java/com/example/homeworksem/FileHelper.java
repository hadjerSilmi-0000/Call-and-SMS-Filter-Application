package com.example.homeworksem;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static List<String> readNumbers(Context context, String filename) {
        List<String> numbers = new ArrayList<>();
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) return numbers;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    numbers.add(line);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numbers;
    }

    public static boolean isBlacklisted(Context context, String number) {
        List<String> blacklist = readNumbers(context, "blacklist.txt");
        return containsNumber(blacklist, number);
    }

    public static boolean isWhitelisted(Context context, String number) {
        List<String> whitelist = readNumbers(context, "whitelist.txt");
        if (whitelist.isEmpty()) return true; // if whitelist empty allow everyone
        return containsNumber(whitelist, number);
    }

    private static boolean containsNumber(List<String> list, String number) {
        for (String n : list) {
            if (n.equals(number)) return true;
            // normalize: compare without country code
            if (number.startsWith("+213") && n.equals("0" + number.substring(4))) return true;
            if (n.startsWith("+213") && number.equals("0" + n.substring(4))) return true;
        }
        return false;
    }
}