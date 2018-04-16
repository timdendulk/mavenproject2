package Impl;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Impl.Implementatie;

public class BrowserHelper {
    private WebDriver webDriver;
    private WebDriverWait wait;
    private WebDriverWait waitlong;


    BrowserHelper(){

        String os = System.getProperty("os.name");
        if("Mac OS X".equals(os)) {
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver");
        } else {
            System.setProperty("webdriver.chrome.driver","drivers/chromedriver.exe");//"c:/cucumberproject/chromedriver.exe");

        }
        webDriver = new ChromeDriver();
        wait = new WebDriverWait(webDriver,10);
        waitlong = new WebDriverWait(webDriver,20);
    }

    public void gaNaarUrl(String s) {
        webDriver.navigate().to(s);

    }

    public void klikelement(String s) {
//        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(s)));
//        webDriver.findElement(By.cssSelector(s)).click();
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(s)));
        webDriver.findElement(By.cssSelector(s)).click();
    }
    public void assertion(String s){
        Assert.assertTrue("cars input field not loaded",webDriver.findElement(By.cssSelector(s))!=null);
    }
    public void typetekst(String s, String input){
        webDriver.findElement(By.cssSelector(s)).sendKeys();
    }
    public void klikindropdown(String s){
        //WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(s)));
        //webDriver.findElement(By.xpath(s)).click();
        WebElement element2 = webDriver.findElement(By.xpath(s));
        System.out.println(element2.getText());

    }

    public void nuttelozeMethode(){
        System.out.println("deze methode mag zo weer weggegooid worden.");
    }
}
