package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Bubba on 3/4/2015.
 */
public class MoibileCinemaCitySiteHandler {
    static private List<String> UrlList;
    static Map<String, String> mainSiteCookies;

    public static HtmlPage GetAvaialbeSeats() throws IOException, InterruptedException {
        // write your code here
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        CookieManager cookieManager = webClient.getCookieManager();
        cookieManager.setCookiesEnabled(true);
        String url ="http://m.cinema-city.co.il/movieBrowser";
        HtmlPage page = null;
        int i = 0;
        try {
            page = webClient.getPage(url);
            i = webClient.waitForBackgroundJavaScript(2000);
        } catch (Exception e) {
            System.out.println("Get page error");
        }
        JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();
        while (manager.getJobCount() > 0) {
            Thread.sleep(1000);
        }

        while (i > 0)
        {
            i = webClient.waitForBackgroundJavaScript(1000);

            if (i == 0)
            {
                break;
            }
            synchronized (page)
            {
                System.out.println("wait");
                page.wait(500);
            }
        }

        webClient.getAjaxController().processSynchron(page,null,false);

        System.out.println(page.asXml());
        return page;
    }
}

