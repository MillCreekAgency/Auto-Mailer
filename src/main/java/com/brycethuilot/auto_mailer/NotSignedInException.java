package com.brycethuilot.auto_mailer;

import java.util.HashMap;

/**
 * Created so that if QQ is not signed in and a developer tries to call {@link QQUpdater#getEmail(String)} or {@link QQUpdater#updatePolicy(String, String, double, HashMap, int[], boolean)} it will throw and expection
 */
public class NotSignedInException extends Exception {

    /**
     * Returns the message why it failed
     * @return the reason it was thrown as a string
     */
    @Override
    public String getMessage() {
        return "WebDriver was not signed into QQ Catalyst";
    }


}
