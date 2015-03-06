package com.company;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Bubba on 3/4/2015.
 */
public class MainCinemaCitySiteHandler {
    private List<String> UrlList = new ArrayList<String>();
    Map<String, String> mainSiteCookies= new HashMap<String, String>();

    public void GetAvaialbeSeats() throws IOException, InterruptedException {
        // write your code here
        Connection.Response mainSiteResponse = Jsoup.connect("http://www.cinema-city.co.il/").method(Connection.Method.GET).execute();
        mainSiteResponse.cookies(); //Get the ses

        String mainSiteBody = mainSiteResponse.body();
        Document parse = Jsoup.parse(mainSiteBody);

        Elements select = parse.select("a[href][onclick^=openFancy]");
        Pattern pattern = Pattern.compile("(openFancyBoxExternal\\('http://tickets.cinema-city.co.il.*?'\\))");
        Matcher matcher;
        for (final Element item : select) {
            matcher = pattern.matcher(item.toString());
            if (matcher.find() && item.toString().contains("סינמה")) {
                String foundUrl = matcher.group(1);
                String edittedUrl = foundUrl.replace("openFancyBoxExternal('", "").replace("')", "").replace("&amp;", "&");
                UrlList.add(edittedUrl);
                System.out.println("blkanbld " + edittedUrl);
            }
        }

        for (final String url : UrlList) {
            GetMovieList(url);
        }
    }

    private void GetMovieList(String url) throws IOException, InterruptedException {
        Map<String, String> ticketSiteCookies;
        //   Connection.Response response4 = Jsoup.connect(url)
        Connection.Response response4 = Jsoup.connect("http://tickets.cinema-city.co.il/webtixsnetglilot/newsessionredirector.aspx?redirect=SelectBusinessDatePage.aspx&key=gli")
                .cookies(mainSiteCookies).method(Connection.Method.GET).execute();
        ticketSiteCookies = response4.cookies();
        Pattern pattern = Pattern.compile("(http://tickets.cinema-city.co.il/.*/)");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            return;
        }
        String baseTicketingUrl = matcher.group(1);

        String datedTicketingUrl = GetDatedTicketingUrl(baseTicketingUrl, new Date());
        Document doc = Jsoup.connect(datedTicketingUrl)
                .cookies(ticketSiteCookies)
                .followRedirects(true)
                .get();

        Element blah = doc.body();

        Elements htmlContent = doc.select("a.CinemaSelectEventPage_Table_EventMasterCell");
        for (final Element item : htmlContent) {
            System.out.println(item.text());
        }

        final WebClient webClient = new WebClient();
        CookieManager cookieManager = webClient.getCookieManager();
        cookieManager.setCookiesEnabled(true);

        for (Map.Entry<String, String> entry : ticketSiteCookies.entrySet()) {
            Cookie cookie = new Cookie("tickets.cinema-city.co.il", entry.getKey(), entry.getValue());
            cookieManager.addCookie(cookie);
        }

        webClient.setCookieManager(cookieManager);
        HtmlPage page2 = webClient.getPage(datedTicketingUrl);
        webClient.setJavaScriptTimeout(5000);
        List<HtmlAnchor> byXPath = (List<HtmlAnchor>) page2.getByXPath("//a[@class='CinemaSelectEventPage_Events_DateTimeCell_Link']");


        for (HtmlAnchor link : byXPath) {
            Thread.sleep(500);
            page2 = webClient.getPage(datedTicketingUrl);
            String script = link.getHrefAttribute();
            if (!script.contains("javascript"))
                return;
            HtmlPage newPage = link.click();


            List<HtmlAnchor> movieName = (List<HtmlAnchor>) newPage.getByXPath("//a[@class='General_Result_Text']");
            if (movieName.size() > 0)
                System.out.println(movieName.get(0).getFirstChild().toString());
            List<HtmlSpan> timeOfDay = (List<HtmlSpan>) newPage.getByXPath("//span[@class='SessionInformation_Label_DateTime']");
            if (timeOfDay.size() > 0)
                System.out.println(timeOfDay.get(0).getFirstChild().toString());
            List<HtmlDivision> byXPath1 = (List<HtmlDivision>) newPage.getByXPath("//div[@class='seat']");

            int lastRownum=1;
            for (HtmlDivision element : byXPath1) {

                int rownum = Integer.parseInt(element.getId().replace("_Seat_", "").substring(0,1));
                //String message = "Seat " + element.getId().replace("_Seat_", "") + " is ";
                String message = "G";
                if (element.toString().contains("SeatStatus=1")) {
                    //message += "Vacant";
                    message ="O";
                } else {
                    message = "X";
                }
                System.out.print(message);
                if (lastRownum!=rownum)
                {
                    lastRownum = rownum;
                    System.out.println();
                }
            }
        }
    }

    private static String GetDatedTicketingUrl(String mainUrl, Date chosenDate) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(date);
        return mainUrl + "TicketingTodaysEventsPage.aspx?BusinessDate=" + formattedDate;
    }
}

