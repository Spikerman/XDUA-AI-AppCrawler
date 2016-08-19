import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenhao on 8/19/16.
 */

public class AppStorePageProcessor implements PageProcessor {

    public static String storeLinkForMI = "http://app.xiaomi.com/details?id=%s";
    public String store;
    public String packageName;

    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(20).setSleepTime(2000).setTimeOut(150000)
            .setCharset("utf-8")
            .setUserAgent("iTunes/12.2.1 (Macintosh; Intel Mac OS X 10.11.3) AppleWebKit/601.4.4")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh; q=0.50");

    public AppStorePageProcessor(String store, String packageName) {
        this.store = store;
        this.packageName = packageName;
    }


    public static void main(String[] args) {
        String pname = "com.meitu.meipaimv";
        String store = "XIAOMI";
        String appPageLink = String.format(storeLinkForMI, pname);
        Spider.create(new AppStorePageProcessor(pname, store))
                .addUrl(appPageLink)
                .thread(1)
                .run();
    }


    @Override
    public void process(us.codecraft.webmagic.Page page) {
        AppInfo appInfo = new AppInfo(packageName);
        switch (store) {
            case "XIAOMI":
                pageParserForXIAOMI(page, appInfo);
                break;
        }

        page.putField("appinfo", appInfo);
    }

    @Override
    public Site getSite() {
        return site;
    }


    //小米应用商店解析
    private void pageParserForXIAOMI(us.codecraft.webmagic.Page page, AppInfo appInfo) {
        appInfo.company = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/p[1]/text()").toString();
        appInfo.cname = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/h3[1]/text()").toString();
        appInfo.imgUrl = page.getHtml().xpath("//div[@class=app-info]/img[1]/@src").toString();
        String apkSizeString = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[1]/li[2]/text()").replace("M", "").toString();
        appInfo.apkSize = (long) Float.parseFloat(apkSizeString) * 1024 * 1024;
        appInfo.version = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[1]/li[4]/text()").toString();
        appInfo.versionDate = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[1]/li[6]/text()").toString();
        appInfo.cato = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/p[2]/text()").replace("手机", "").toString();

        //权限列表处理
        List<String> permissionStringList = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[@class=second-ul]/li").all();
        appInfo.permissionList = new ArrayList<>();
        Pattern p = Pattern.compile("(<li>)|(</li>)|▪");
        Matcher m;
        for (String x : permissionStringList) {
            m = p.matcher(x);
            if (m.find())
                appInfo.permissionList.add(m.replaceAll("").trim());
        }

        //处理评论数量字符串
        String ratingCountString = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/span[@class=app-intro-comment]/text()").toString();
        p = Pattern.compile("[1-9]+");
        m = p.matcher(ratingCountString);
        while (m.find()) {
            appInfo.ratingCount = Integer.parseInt(m.group());
        }

        //处理评分字符串
        String ratingString = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/div[@class=star1-empty]/div[1]/@class").toString();
        p = Pattern.compile("star1-hover star1-");
        m = p.matcher(ratingString);

        appInfo.rating = Float.parseFloat(m.replaceAll("")) / 2;
        appInfo.brief = page.getHtml().xpath("//p[@class=pslide]/text()").toString();
    }
}
