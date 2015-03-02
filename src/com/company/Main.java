package com.company;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static Map<String, String> mainSiteCookies;
    static List<String> UrlList = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        // write your code here
        Connection.Response mainSiteResponse = Jsoup.connect("http://www.cinema-city.co.il/").method(Connection.Method.GET).execute();
        mainSiteCookies = mainSiteResponse.cookies(); //Get the ses

        String mainSiteBody = mainSiteResponse.body();
        Document parse = Jsoup.parse(mainSiteBody);

        Elements select = parse.select("a[href][onclick^=openFancy]");
        Pattern pattern = Pattern.compile("(openFancyBoxExternal\\('http://tickets.cinema-city.co.il.*?'\\))");
        Matcher matcher;
        for (final Element item : select) {
            matcher = pattern.matcher(item.toString());
            if (matcher.find() && item.toString().contains("סינמה")) {
                String foundUrl = matcher.group(1);
                String edittedUrl = foundUrl.replace("openFancyBoxExternal('","").replace("')","").replace("&amp;","&");
                UrlList.add(edittedUrl);
                System.out.println("blkanbld " + edittedUrl);
            }
        }

        for(final String url : UrlList)
        {
            GetMovieList(url);
        }





    }

    private static void GetMovieList(String url) throws IOException {
        Map<String, String> ticketSiteCookies;
     //   Connection.Response response4 = Jsoup.connect(url)
        Connection.Response response4 = Jsoup.connect("http://tickets.cinema-city.co.il/webtixsnetglilot/newsessionredirector.aspx?redirect=SelectBusinessDatePage.aspx&key=gli")
                .cookies(mainSiteCookies).method(Connection.Method.GET).execute();
        ticketSiteCookies = response4.cookies();
        Pattern pattern = Pattern.compile("(http://tickets.cinema-city.co.il/.*/)");
        Matcher matcher= pattern.matcher(url);
        if (!matcher.find())
        {
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
    }

    private static String GetDatedTicketingUrl(String mainUrl, Date chosenDate)
    {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(date);
        return mainUrl + "TicketingTodaysEventsPage.aspx?BusinessDate=" + formattedDate;
    }


}