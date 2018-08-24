package com.brycethuilot.auto_mailer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    private static final String fileName = "../config/settings.txt";

    Config() throws IOException {
        HashMap<String, String> settings  =this.readConfig();
        Email.setSettings(settings);
        ApplicationWindow.setSetting(settings);
    }

    HashMap<String, String> readConfig() throws IOException{
        File file = new File(fileName);
        Scanner sc = new Scanner(file);
        HashMap<String, String> settings = new HashMap<>();

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] setting = line.split("=");
            settings.put(setting[0], setting[1]);
        }


        return settings;
    }

    void setConfig(HashMap<String, String> hashMap) throws IOException {
        StringBuilder settings = new StringBuilder();
        for(String settingName : hashMap.keySet()) {
            settings.append(settingName);
            settings.append("=");
            settings.append(hashMap.get(settingName));
            settings.append("\n");
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(settings.toString());

        writer.close();
    }
}
