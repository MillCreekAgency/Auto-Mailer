package com.brycethuilot.auto_mailer;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Config class saves settings in users home directory.
 * Currently the settings that can be adjusted are
 * <ul>
 *     <li><b>From email:</b> The email to send from when sending to insured</li>
 *     <li><b>From name:</b> The name the email says it came from</li>
 *     <li><b>Default QQ Username:</b> the default qq username to be in the login form</li>
 *     <li><b>SMTP Port:</b> the port to send the SMTP request to</li>
 *     <li><b>SMTP Host:</b> the host to send the SMTP request to</li>
 *     <li><b>Remote Mode:</b> if instead of printing for mortgage and printing letter, send to an email <i>Needs to be added in version 1.1.0</i></li>
 * </ul>
 */
public class Config {

    private static final String SETTINGS_DIRECTORY = System.getProperty("user.home") + "/.MillCreek/";
    private static final String FILE_NAME = SETTINGS_DIRECTORY + "settings.txt";

    /**
     * Creates an Config object adn reads from the settings file and sets it to all the classes
     * @throws IOException if the settings file could not be read
     */
    Config() throws IOException {
        this.firstTimeSetUp();
        this.setConfig();
    }

    /**
     * Sets the config settings for classes
     * @throws IOException if the file could not be read
     */
    private void setConfig() throws IOException{
        HashMap<String, String> settings  = this.readConfig();
        Email.setSettings(settings);
        ApplicationWindow.setSetting(settings);
        Policy.setConfig(settings);
    }

    /**
     * Creates a new settings file in the .MillCreek directory in the user home, if one does not exist
     */
    private void firstTimeSetUp() {
        File settings = new File(FILE_NAME);
        if(!settings.exists()) {
            try {
                new File(SETTINGS_DIRECTORY).mkdirs();
                settings.createNewFile();
                FileWriter settingsFile =  new FileWriter(settings);
                settingsFile.write("From_Email=bryce@millcreekagency.com\n" +
                        "Port=587\n" +
                        "Default_QQ_Username=dean@millcreekagency.com\n" +
                        "SMTP_Host=smtp.office365.com\n" +
                        "From_Name=Bryce Thuilot\n" +
                        "Remote_mode=false\n" +
                        "Remote_Email=bryce@millcreekagency.com");
                settingsFile.flush();
                settingsFile.close();
            } catch (IOException io){
                System.out.println("could not create settings file");
                System.exit(1);
            }
        }
    }

    /**
     * Reads the config file
     * @return a hash mapping the setting names to the setting value
     * @throws IOException if the config file can't be read
     */
    HashMap<String, String> readConfig() throws IOException{
        File file = new File(FILE_NAME);
        Scanner sc = new Scanner(file);
        HashMap<String, String> settings = new HashMap<>();

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] setting = line.split("=");
            settings.put(setting[0], setting[1]);
        }

        return settings;
    }

    /**
     * Writes a hash to the settings file
     * @param hashMap a hash mapping setting
     * @throws IOException if the config file cannot be written to
     */
    void setConfig(HashMap<String, String> hashMap) throws IOException {
        StringBuilder settings = new StringBuilder();
        for(String settingName : hashMap.keySet()) {
            settings.append(settingName);
            settings.append("=");
            settings.append(hashMap.get(settingName));
            settings.append("\n");
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
        writer.write(settings.toString());

        writer.close();
        this.setConfig();
    }
}
