import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestOutScenarioTests {

    private WebDriver driver;
    private final String baseUrl = "http://testoutlivecontent.blob.core.windows.net/secpro2017v6-en-us/en-us/sims/windows/xsimengine.html?simpackage=windows&simfile=email_social_eng_secpro6.html&dev=true&automation=true";
    private List<EmailItem> mailToDelete = new ArrayList<EmailItem>();

    @Before
    public void setUp() {
        // Add the Geckodriver location to your system PATH (preferred) or fill out below:
        // System.setProperty("webdriver.gecko.driver", "...\\geckodriver.exe");

        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // specify emails the tests should delete
        mailToDelete.add(new EmailItem("New Service Pack", "Microsoft Windows Update Center"));
        mailToDelete.add(new EmailItem("Payment Pending", "Online Banking Department"));
        mailToDelete.add(new EmailItem("FW: FW: FW: Virus Attack Warning", "Grandma Jacklin "));
        mailToDelete.add(new EmailItem("Web Site Update", "Emily Smith"));
        mailToDelete.add(new EmailItem("Wow!!", "Sara Goodwin"));
        mailToDelete.add(new EmailItem("7 Yr Old with Cancer", "Grandma Jacklin"));
        mailToDelete.add(new EmailItem("Re: Lunch Today?", "Joe Davis"));
        mailToDelete.add(new EmailItem("Executive Jobs", "Executive Recruiting"));
    }

    /**
     * Opens a test
     * Some waiting must occur for the application to load before a test may be run.
     * Here we wait for the progress bar, and then for the mail client
     */
    private void loadTest()
    {
        // GET the simulation app
        driver.get(baseUrl);
        // Wait for progress bar page to begin
        WebElement progressBarElement = (new WebDriverWait(driver, 60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("progresstip_span")));
        System.out.println("Progress bar displayed...");
        // Wait for Scenario to load
        WebElement scenarioDynamicElement = (new WebDriverWait(driver, 60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("tbSimType")));
        System.out.println("Scenario Loaded! Loading email client...");
        // Wait for mail client to load by looking for the email list
        WebElement listBoxElement = (new WebDriverWait(driver, 60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("mlbMailListBox.Grid.Border.ScrollViewer.Border.Panel.Viewport.ScrollContentPresenter.OuterElement")));
        System.out.println("Finished loading.");
    }

    @Test
    public void testDeleteEmailsWithCheckbox()
    {
        loadTest();

        Actions action = new Actions(driver);
        WebElement outerElement = driver.findElement(By.id("mlbMailListBox.Grid.Border.ScrollViewer.Border.Panel.Viewport.ScrollContentPresenter.OuterElement"));

        int legitMailRead = 0;  // track how many emails existing we have examined
        List<WebElement> emails = outerElement.findElements(By.xpath("//div[@data-typename='MailItem']"));
        for (int i = legitMailRead; i < emails.size(); i++) {
            action.moveToElement(emails.get(i)).click().build().perform();
            EmailItem currentEmail = new EmailItem(emails.get(i));
            if (mailToDelete.contains(currentEmail)) {
                // get and click checkbox within email item
                WebElement checkboxElement = emails.get(i).findElement(By.xpath(".//div[@data-typename='CheckBox']"));
                action.moveToElement(checkboxElement).click().build().perform();
                // get and click delete button
                WebElement deleteElement = driver.findElement(By.xpath("//span[.='Delete']"));
                action.moveToElement(deleteElement).click().build().perform();
                // refresh the list of emails, and don't iterate forward
                emails = outerElement.findElements(By.xpath("//div[@data-typename='MailItem']"));
                i--;
            } else {
                legitMailRead++;
            }
        }

        // Press the done button
        WebElement buttonElement = driver.findElement(By.id("bDone"));
        action.moveToElement(buttonElement).click().build().perform();

        // Assert we passed
        WebElement resultsPanelElement = driver.findElement(By.xpath("//div[@id='svResults']"));
        WebElement resultsElement = resultsPanelElement.findElement(By.xpath(".//div[contains(.,'Your Score')]"));
        boolean pass = resultsElement.getText().contains("100%");
        Assert.assertTrue(pass);
    }

    @Test
    public void testDeleteEmailsWithX()
    {
        loadTest();

        Actions action = new Actions(driver);
        WebElement outerElement = driver.findElement(By.id("mlbMailListBox.Grid.Border.ScrollViewer.Border.Panel.Viewport.ScrollContentPresenter.OuterElement"));

        int legitMailRead = 0;  // track how many emails existing we have examined
        List<WebElement> emails = outerElement.findElements(By.xpath("//div[@data-typename='MailItem']"));
        for (int i = legitMailRead; i < emails.size(); i++) {
            action.moveToElement(emails.get(i)).click().build().perform();

            EmailItem currentEmail = new EmailItem(emails.get(i));
            if (mailToDelete.contains(currentEmail)) {
                // get and mouseover the gray X (wh
                WebElement xIconElement = emails.get(i).findElement(By.xpath(".//img[contains(@src,'deletegray.png')]"));
                action.moveToElement(xIconElement).build().perform();
                // click the X
                action.click().build().perform();
                // refresh the list of emails, and don't iterate forward
                emails = outerElement.findElements(By.xpath("//div[@data-typename='MailItem']"));
                i--;
            } else {
                legitMailRead++;
            }
        }

        // Press the done button
        WebElement buttonElement = driver.findElement(By.id("bDone"));
        action.moveToElement(buttonElement).click().build().perform();

        // Assert we passed
        WebElement resultsPanelElement = driver.findElement(By.xpath("//div[@id='svResults']"));
        WebElement resultsElement = resultsPanelElement.findElement(By.xpath(".//div[contains(.,'Your Score')]"));
        boolean pass = resultsElement.getText().contains("100%");
        Assert.assertTrue(pass);
    }
}

/**
 * EmailItem is a simple class for specifying emails for deletion.
 * Its constructors accept either a WebElement of a MailItem
 * or hardcoded strings of subject and sender.
 *
 * Note: it would be better to also include the datetime stamp if we could,
 * but the simulation seems to be choosing different times for today.
 * It seems to be picking mail times at a relative interval before the current time,
 * but I don't find it wise to implement that without having a guarantee.
 *
 * @author Robert Thorne
 * @since 1.0
 */
class EmailItem {

    private WebElement webElement;
    private String subject;
    private String sender;

    EmailItem(WebElement webElement) {
        this.webElement = webElement;
        WebElement descriptionElement = webElement.findElement(By.xpath(".//div[contains(@id,'tbDescription')]"));
        this.subject = descriptionElement.getText();
        WebElement senderElement = webElement.findElement(By.xpath(".//div[contains(@id,'tbSender')]"));
        this.sender = senderElement.getText();
    }

    EmailItem(String subject, String from) {
        this.sender = from;
        this.subject = subject;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof EmailItem) {
            EmailItem email2 = (EmailItem) object;
            return sender.equals(email2.sender) && subject.equals(email2.subject);
        } else
            return false;
    }
}