package com.brycethuilot.auto_mailer;

public class NotSignedInException extends Exception {

    @Override
    public String getMessage() {
        return "WebDriver was not signed into QQ Catalyst";
    }


}
