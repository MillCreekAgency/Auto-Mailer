package com.brycethuilot.auto_mailer;// Selenium Imports
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
// Util imports
import java.util.HashMap;
import java.util.List;

/**
 * Class used to open a chrome window controlled by a web driver
 * can update a policy and get email assocated with user on QQCatalyst.com
 *
 * @author Bryce Thuilot
 * @version %I%, %G%
 * @since 1.0
 */
public class QQUpdater {
    private String email;
    private String login;
    private String password;
    private WebDriver driver;
    private boolean checkedEmail;

    /**
     * Creates a chrome window and logs in into QQCatalyst with given information
     * @param login the email to use to login
     * @param password the password to use to login
     */
    public QQUpdater(String login, String password){
        this.login = login;
        this.password = password;
        driver = new ChromeDriver();
        driver.navigate().to("https://app.qqcatalyst.com/Contacts/Search");
        this.login();
    }

    /**
     * Closes WebDriver (should be called when done updating)
     */
    public void close(){
        driver.close();
        driver.quit();
    }

    /**
     * Gets the email from a given policy number
     * @param policyNum policy number of policy to navigate to
     * @return the string of the email found, null if nothing found
     */
    public String getEmail(String policyNum){
        if(this.checkedEmail) {
            return this.email;
        }

        sleep(2);
        try{
          driver.findElement(By.className("wm-close-button")).click();
          sleep(2);
        } catch(org.openqa.selenium.NoSuchElementException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        try{
            driver.findElement(By.className("walkme-click-and-hover")).click();
            sleep(2);
        } catch(org.openqa.selenium.NoSuchElementException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
        // Enter old policy number into search bar
        driver.findElement(By.id("contact-search-text")).sendKeys(policyNum);
        // Click go for search
        driver.findElement(By.id("contact-search-go")).click();
        this.sleep(3);
        List<WebElement> searchResults = driver.findElements(By.tagName("a"));

        for(WebElement link : searchResults) {
            if (link.getAttribute("innerHTML").contains(policyNum) && link.isDisplayed()) {
                link.click();
                break;
            }
        }

        this.sleep(2);

        this.checkedEmail = true;
        try {
            WebElement email = driver.findElement(By.className("overview-email-link"));
            this.email = email.getAttribute("innerHTML");
            return this.email;
        } catch(org.openqa.selenium.NoSuchElementException ex) {
            return null;
        }

    }

    /**
     * Updates a policy on QQ catalyst with given information
     * @param policyNum new policy number of policy
     * @param oldPolicyNumber policy number of policy to update
     * @param premium premium of the policy
     * @param coverages coverages of policy mapped to there CSS option value
     * @param deductibles deductible and hurricane deductible in an array
     * @param dwelling whether the policy is dwelling or not
     */
    public void updatePolicy(String policyNum, String oldPolicyNumber, double premium, HashMap<Integer, Double> coverages, int[] deductibles, boolean dwelling) {
        if(!driver.getCurrentUrl().contains("https://app.qqcatalyst.com/Policies/Policy/Details/")) {
            this.getEmail(oldPolicyNumber);
        }
        WebElement renewalButton = driver.findElement(By.className("PolicyActionRenew"));
        while (!renewalButton.isDisplayed()) {
            List<WebElement> labels = driver.findElements(By.className("Label"));

           this.findButton(labels, "Policy Action");
        }

        renewalButton.click();

        this.sleep(2);

        List<WebElement> submitYes = driver.findElements(By.className("yes"));

        this.findButton(submitYes, "Yes");

        String currentUrl = driver.getCurrentUrl();
        while(!currentUrl.contains("New=true&Mode=basic&extra=issue") && !currentUrl.contains("https://app.qqcatalyst.com/Policies/com.brycethuilot.auto_mailer.Policy/Details")) {
            this.sleep(2);
            currentUrl = driver.getCurrentUrl();
        }
        this.sleep(2);

        WebElement policyTab = driver.findElement(By.id("pb-PolicyInfo"));
        while(!policyTab.isDisplayed()) {
            this.sleep(1);
        }
        policyTab.click();

        // TODO improve switching Tabs
        this.sleep(1);

        WebElement policyNumInput = driver.findElement(By.name("PolicyNo"));
        while(!policyNumInput.isDisplayed()) {
            sleep(1);
        }
        policyNumInput.clear();
        policyNumInput.sendKeys(policyNum);

        driver.findElement(By.id("pb-PolicyQuotes")).click();

        WebElement button = driver.findElement(By.className("listBasedContextRow"));

        while(!button.isDisplayed()) {
            this.sleep(1);
        }

        button.click();

        // TODO improve switching Tabs
        sleep(1);

        this.findButton(driver.findElements(By.className("context-default-edit")), "Edit");
        // TODO improve accessing edit tab
        this.sleep(3);

        WebElement premiumInput = driver.findElement(By.name("BasePremium"));
        this.fillNumberInput(premiumInput, premium);
        this.sleep(1);
        driver.findElement(By.name("QuoteSubStatus")).findElement(By.cssSelector("option[value='S']")).click();

        WebElement homeTab = dwelling ? driver.findElement(By.id("pb-HomeLiabilityLimitsDFIRE")) : this.driver.findElement(By.id("pb-HomeLiabilityLimitsHOME"));
        WebElement nextButton = driver.findElement(By.className("basic-next"));

        while(!homeTab.isDisplayed()) {
            nextButton.click();
            sleep(1);
        }
        homeTab.click();

        WebElement home = driver.findElement(By.id("HomeLiabilityLimits"));

        while(!home.isDisplayed()) {
            sleep(1);
        }
        home.click();
        this.sleep(1);

        List<WebElement> coverageInputs = driver.findElements(By.className("SectionItem"));//"PolicyLiabilityCoverageContainer"));
        this.fillCoverages(coverages, coverageInputs);

        int i = 0;
        List<WebElement> deductibleInputs = driver.findElements(By.name("Amount"));
        for(WebElement input : deductibleInputs) {
            if(!input.isDisplayed()) {
                continue;
            } else {
                this.fillNumberInput(input, deductibles[i]);
                i++;
                if(i >= deductibles.length) {
                    break;
                }
            }
        }
        List<WebElement> finishButtons = driver.findElements(By.className("basic-page-fin"));
        this.findButton(finishButtons, "Finish");

        this.sleep(3);

        try {
            WebElement popup = driver.findElement(By.id("simplemodal-container"));
            popup.findElement(By.className("submit")).click();
        }catch(org.openqa.selenium.NoSuchElementException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        while(!driver.getCurrentUrl().contains("Workflows/IssueMultiPolicy")) {
            this.sleep(1);
        }
        this.sleep(2);

        driver.findElement(By.name("PremiumSent")).findElement(By.cssSelector("option[value='G'")).click();

        this.findButton(driver.findElements(By.className("finish")), "Finish");

        this.sleep(2);

        this.findButton(driver.findElements(By.className("btnYes")), "Yes");

        WebElement returnToPolicy = driver.findElement(By.className("returntoentity"));
        while(!returnToPolicy.isDisplayed()) {
            sleep(1);
        }
        returnToPolicy.click();
        this.sleep(1);
    }

    /**
     * Fille the coverage inputs
     * @param coverages coverages mapped to there CSS option
     * @param inputs each row of the input for coverages
     */
    private void fillCoverages(HashMap<Integer, Double> coverages, List<WebElement> inputs) {
        for(WebElement currentInput : inputs) {
            try {
                WebElement selector = currentInput.findElement(By.name("CoverageID"));
                Select coverageSelect = new Select(selector);
                WebElement input = currentInput.findElement(By.name("CovLimDed"));
                if(coverageSelect.getFirstSelectedOption().getAttribute("value") == null){
                    return;
                }
                double coverage = coverages.get(Integer.parseInt(coverageSelect.getFirstSelectedOption().getAttribute("value")));
                if(input.isDisplayed()) {
                    fillNumberInput(input, coverage);
                }
            }catch (org.openqa.selenium.NoSuchElementException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Fill a input with a double
     * @param element element to send keys to
     * @param number number to fill in
     */
    private void fillNumberInput(WebElement element, double number) {
        element.clear();
        element.sendKeys(number + "");
    }


    /**
     * Fill a input with an int
     * @param element element to send keys to
     * @param number number to fill in
     */
    private void fillNumberInput(WebElement element, int number) {
        element.clear();
        element.sendKeys(number + "");
    }

    /**
     * Finds a button in a list of buttons with given text that is displayed
     * @param buttons List of buttons to search
     * @param text text for button to have
     */
    private void findButton(List<WebElement> buttons, String text) {
        for(WebElement button : buttons) {
            if(button.getAttribute("innerHTML").contains(text) && button.isDisplayed()) {
                button.click();
                break;
            }
        }
    }


    /**
     * Logs into qq catalyst
     */
    private void login() {
        if(driver.getCurrentUrl().contains("login.qqcatalyst.com")) {
            driver.findElement(By.name("txtEmail")).sendKeys(login);
            driver.findElement(By.id("txtPassword")).sendKeys(password);
            driver.findElement(By.id("lnkSubmit")).click();
            this.sleep(1);
            if (driver.getCurrentUrl().contains("login.qqcatalyst.com")) {
                driver.findElement(By.id("lnkCancel")).click();
            }
        }
    }

    /**
     * Has program sleep for a given number of seconds
     * @param seconds amount of seconds to sleep
     */
    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }
    }
}
