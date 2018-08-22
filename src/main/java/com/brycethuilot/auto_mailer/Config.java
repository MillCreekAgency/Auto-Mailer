package com.brycethuilot.auto_mailer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    Config() throws IOException {
        HashMap<String, String> settings  =this.readConfig();
        Email.setSettings(settings);
        ApplicationWindow.setSetting(settings);
    }

    HashMap<String, String> readConfig() throws IOException{
        File file = new File("config/settings.txt");
        Scanner sc = new Scanner(file);

        HashMap<String, String> settings = new HashMap<>();

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] setting = line.split("=");
            settings.put(setting[0], setting[1]);

        }

        return settings;
    }

    void setConfig(HashMap<String, String> hashMap) {

    }
}
