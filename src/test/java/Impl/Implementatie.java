package Impl;

public class Implementatie {

BrowserHelper driver = new BrowserHelper();

    public void gaNaarDeWebsite() {
        //String testwebsite = "file://"+ BestandHelper.getRootPath() + File.separator + "src/main/cuke_workshop/leningaanvraag.html";
        driver.gaNaarUrl("https://www.phptravels.net/");
    }

    public void clickonbutton(String s) {
        driver.klikelement(s);

    }

    public void checkiftrue(String s) {
        driver.assertion(s);
    }
    public void typetext(String s, String input) {
        driver.typetekst(s,input);
    }
    public void clickindropdown(String s){
        driver.klikindropdown(s);
    }



}
