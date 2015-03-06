package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bubba on 3/4/2015.
 */
public class LevSiteHandler {
    private List<String> UrlList = new ArrayList<String>();
    Map<String, String> mainSiteCookies = new HashMap<String, String>();
    private List<String> UrlSitList = new ArrayList<String>();


    public void GetAvaialbeSeats() throws IOException, InterruptedException {
        // write your code here
        Connection.Response mainSiteResponse = Jsoup.connect("http://www.lev.co.il/movies").method(Connection.Method.GET).execute();
        mainSiteCookies = mainSiteResponse.cookies(); //Get the ses

        String mainSiteBody = mainSiteResponse.body();
        Document parse = Jsoup.parse(mainSiteBody);

        Elements select = parse.select("div[class=movieName]");
        for (final Element movieName : select) {
            System.out.println(movieName.childNode(0));
            Element link = movieName.parent().parent();
            if (link.tagName() == "a" && link.attributes().hasKey("href")) {
                link.setBaseUri("http://www.lev.co.il/");
                String movieSitUrl = link.attr("abs:href");
                UrlList.add(movieSitUrl);
            }
        }

        for (final String movieUrl : UrlList) {
            GetTimes(movieUrl);
        }


        for (final String sitUrl : UrlSitList) {
            GetSits(sitUrl);
        }
    }

    private void GetSits(String sitUrl) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        webClient.getPage("http://www.lev.co.il/movies");
        CookieManager cookieManager = webClient.getCookieManager();
        cookieManager.setCookiesEnabled(true);
        webClient.waitForBackgroundJavaScript(5000);
        for (Map.Entry<String, String> entry : mainSiteCookies.entrySet()) {
            Cookie cookie = new Cookie("www.lev.co.il", entry.getKey(), entry.getValue());
            cookieManager.addCookie(cookie);
        }
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.waitForBackgroundJavaScriptStartingBefore(10*1000);
        webClient.setCookieManager(cookieManager);
        HtmlPage page = webClient.getPage(sitUrl);
        final List<FrameWindow> window = page.getFrames();
        final HtmlPage pageTwo = (HtmlPage) window.get(0).getEnclosedPage();
        webClient.getCookieManager().getCookies();
        page = webClient.getPage(sitUrl);
    }

    private void GetTimes(String url) throws IOException {
        Connection.Response tempSitPage = Jsoup.connect(url).method(Connection.Method.GET).cookies(mainSiteCookies).execute();
        String sitSiteBody = tempSitPage.body();
        Document parsedSitSiteBody = Jsoup.parse(sitSiteBody);

        Element scheduleDiv = parsedSitSiteBody.select("div[class=schedulePurches]").first();
        Elements links =scheduleDiv.select("a[href]");
        for(Element l : links) {
            l.setBaseUri("http://www.lev.co.il/");
            UrlSitList.add(l.attr("abs:href"));

        }
    }
}

