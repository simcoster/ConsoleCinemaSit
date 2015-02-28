package com.company;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static Map<String, String> cookies ;

    public static void main(String[] args) throws IOException {
        // write your code here
        Connection.Response response3 = Jsoup.connect("http://www.cinema-city.co.il/").method(Connection.Method.GET).execute();
        cookies = response3.cookies(); //Get the ses
         Jsoup.connect("http://www.cinema-city.co.il/").cookies(cookies)
                .followRedirects(true)
                .get();
        String body2 = response3.body();
        Document parse = Jsoup.parse(body2);
        Elements select = parse.select("a[href][onclick^=openFancy]");
        Pattern pattern = Pattern.compile("(openFancyBoxExternal'http://.*?')");
        Matcher matcher;
        for (final Element item : select) {
            matcher = pattern.matcher(item.toString());
            if (matcher.find()) {
                String foundUrl = matcher.group(1);
                System.out.println("blkanbld " + foundUrl);
                //   GetMovieList(foundUrl.replace("'",""));
            }
        }

        Connection.Response response4 = Jsoup.connect("http://tickets.cinema-city.co.il//webtixsnetglilot/newsessionredirector.aspx?redirect=SelectBusinessDatePage.aspx&key=gli")
                .cookies(cookies).method(Connection.Method.GET).execute();
        cookies = response4.cookies();

        //Connection.Response response2 = Jsoup.connect("http://tickets.cinema-city.co.il/webtixsnetglilot/SelectBusinessDatePage.aspx").method(Connection.Method.GET).execute();
        //String body2 = response2.body();
       // cookies = response2.cookies(); //Get the ses
        Document doc = Jsoup.connect("http://tickets.cinema-city.co.il/WebTixsNetGlilot/TicketingTodaysEventsPage.aspx?BusinessDate=2015-2-27")
                .cookies(cookies)
                .followRedirects(true)
                .get();

        Element blah = doc.body();

        Elements htmlContent = doc.select("a.CinemaSelectEventPage_Table_EventMasterCell");
        for (final Element item : htmlContent) {
                System.out.println(item.text());
//            Pattern pattern = Pattern.compile("('.*')");
//            Matcher matcher;
//            for (final Element item2 : htmlContent) {
//                matcher = pattern.matcher(item.toString());
//                if (matcher.find()) {
//                    String foundUrl = matcher.group(1);
//                    System.out.println("blkanbld " + foundUrl);
//                    //   GetMovieList(foundUrl.replace("'",""));
//                }
//            }
                //   GetMovieList(foundUrl.replace("'",""));
        }

    }

    private static void GetMovieList(String url) throws IOException {
        Document doc = Jsoup.connect(url).cookies(cookies).followRedirects(true).get();
        Element blah = doc.body();

    }
}