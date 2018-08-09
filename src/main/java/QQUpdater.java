// Selenium Imports
import javafx.print.PrinterJob;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
// Util imports
/// Print
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class QQUpdater {
    String login;
    String password;
    WebDriver driver;

    public QQUpdater(String login, String password){
        this.login = login;
        this.password = password;
        driver = new ChromeDriver();
        driver.navigate().to("https://app.qqcatalyst.com/Contacts/Search");
        this.login();
    }

    public String getEmail(String oldPolicyNum){
        sleep(2);
        try{
          driver.findElement(By.className("wm-close-button")).click();
          sleep(2);
        } catch(org.openqa.selenium.NoSuchElementException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
        // Enter old policy number into search bar
        driver.findElement(By.id("contact-search-text")).sendKeys(oldPolicyNum);
        // Click go for search
        driver.findElement(By.id("contact-search-go")).click();
        this.sleep(3);
        List<WebElement> searchResults = driver.findElements(By.tagName("a"));

        for(WebElement link : searchResults) {
            if (link.getAttribute("innerHTML").contains(oldPolicyNum) && link.isDisplayed()) {
                link.click();
                break;
            }
        }

        this.sleep(2);
        try {
            WebElement email = driver.findElement(By.className("overview-email-link"));
            return email.getAttribute("innerHTML");
        } catch(org.openqa.selenium.NoSuchElementException ex) {
            return null;
        }

    }

    public void updatePolicy(String policyNum, double premium, HashMap<Integer, Double> coverages, int[] deductibles, boolean dwelling) {
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
        while(!currentUrl.contains("New=true&Mode=basic&extra=issue") && !currentUrl.contains("https://app.qqcatalyst.com/Policies/Policy/Details")) {
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
        driver.close();
    }

    private void fillCoverages(HashMap<Integer, Double> coverages, List<WebElement> inputs) {
        /* TODO fill Coverages based on coverage name */
        for(WebElement currentInput : inputs) {
            try {
                WebElement selector = currentInput.findElement(By.name("CoverageID"));
                Select coverageSelect = new Select(selector);
                WebElement input = currentInput.findElement(By.name("CovLimDed"));
                double coverage = coverages.get(Integer.parseInt(coverageSelect.getFirstSelectedOption().getAttribute("value")));
                fillNumberInput(input, coverage);
            }catch (org.openqa.selenium.NoSuchElementException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
    }

    private void fillNumberInput(WebElement element, double number) {
        element.clear();
        element.sendKeys(number + "");
    }

    private void fillNumberInput(WebElement element, int number) {
        element.clear();
        element.sendKeys(number + "");
    }

    private void findButton(List<WebElement> buttons, String text) {
        for(WebElement button : buttons) {
            if(button.getAttribute("innerHTML").contains(text) && button.isDisplayed()) {
                button.click();
                break;
            }
        }
    }


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

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
        }
    }
}
