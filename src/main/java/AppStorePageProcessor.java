import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenhao on 8/19/16.
 */

public class AppStorePageProcessor implements PageProcessor {

    static String storeLinkForXIAOMI = "http://app.xiaomi.com/details?id=%s";
    static String storeLinkForYYB = "http://sj.qq.com/myapp/detail.htm?apkName=%s";
    static String storeLinkForWDJ = "http://www.wandoujia.com/apps/%s";
    private List<String> appUrlList = new ArrayList<>();
    private boolean isFirstPage = true;
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setCycleRetryTimes(3).setSleepTime(1500).setTimeOut(3000)
            .setCharset("utf-8")
            .setUserAgent("iTunes/12.2.1 (Macintosh; Intel Mac OS X 10.11.3) AppleWebKit/601.4.4")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh; q=0.50");

    public AppStorePageProcessor(String store, String packageName) {
    }

    public AppStorePageProcessor(List<String> appUrlList) {
        this.appUrlList = appUrlList;
    }

    @Override
    public void process(Page page) {
        if (isFirstPage)
            page.addTargetRequests(appUrlList);
        else
            isFirstPage = false;

        AppInfo appInfo = new AppInfo();
        String matchString = "http://app\\.xiaomi\\.com/details\\?id=|http://sj\\.qq\\.com/myapp/detail\\.htm\\?apkName=|http://www\\.wandoujia\\.com/apps/";
        String currentUrl = page.getUrl().toString();
        String storeLink = page.getUrl().regex(matchString).toString();
        appInfo.packageName = currentUrl.replace(storeLink, "");
        String storeName;
        if (storeLink.equals("http://app.xiaomi.com/details?id=")) {
            pageParserForXIAOMI(page, appInfo);
            storeName = "XIAOMI";
        } else if (storeLink.equals("http://sj.qq.com/myapp/detail.htm?apkName=")) {
            pageParserForYYB(page, appInfo);
            storeName = "YYB";
        } else {
            pageParserForWDJ(page, appInfo);
            storeName = "WDJ";
        }

        page.putField("appinfo", appInfo);
        page.putField("storeList", storeName);
    }

    @Override
    public Site getSite() {
        return site;
    }


    //小米应用商店解析
    private void pageParserForXIAOMI(Page page, AppInfo appInfo) {
        appInfo.company = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/p[1]/text()").toString();
        if (appInfo.company == null) {
            System.out.println(Thread.currentThread().getId() + " " + appInfo.packageName + "    NOT FIND in XIAOMI");
            return;
        }
        appInfo.cname = page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/h3[1]/text()").toString();
        appInfo.imgUrl = page.getHtml().xpath("//div[@class=app-info]/img[1]/@src").toString();
        String apkSizeString = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[1]/li[2]/text()").replace("M", "").toString();
        appInfo.apkSize = (long) Float.parseFloat(apkSizeString) * 1024 * 1024;
        appInfo.version = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[1]/li[4]/text()").toString();
        String versionDataString = page.getHtml().xpath("//div[@class=look-detail]/div[1]/ul[1]/li[6]/text()").replace("-", "").toString();
        appInfo.versionDate = Integer.parseInt(versionDataString);
        appInfo.catoList.add(page.getHtml().xpath("//div[@class=app-info]/div[@class=intro-titles]/p[2]/text()").replace("手机", "").toString());

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


    private void pageParserForYYB(Page page, AppInfo appInfo) {
        String ajaxRegexYYB = ".*comment.htm\\?apkName=.*";
        if (page.getUrl().regex(ajaxRegexYYB).match()) {
            //fixme: java.lang.NullPointerException occurs sometimes
            int ratingCount = Integer.parseInt(new JsonPathSelector("$.obj[*].total").select(page.getRawText()));
            page.putField("ratingCount", ratingCount);
        } else {
            appInfo.company = page.getHtml().xpath("//div[@data-modname=appOthInfo]/div[6]/text()").toString();
            if (appInfo.company == null) {
                System.out.println(Thread.currentThread().getId() + " " + appInfo.packageName + "    NOT FIND in YYB");
                return;
            }
            page.addTargetRequest("http://sj.qq.com/myapp/app/comment.htm?apkName=" + appInfo.packageName);// add ajax request for rating count to the fetch queue
            appInfo.cname = page.getHtml().xpath("//div[@class=det-name]/div[@class=det-name-int]/text()").toString();
            appInfo.imgUrl = page.getHtml().xpath("//div[@data-modname=appinfo]/div[@class=det-icon]/img[1]/@src").toString();
            appInfo.version = page.getHtml().xpath("//div[@data-modname=appOthInfo]/div[2]/text()").replace("V|v", "").toString();
            //处理下载量
            String downloadString = page.getHtml().xpath("//div[@class=det-insnum-line]/div[@class=det-ins-num]/text()").toString();//8.3亿下载
            if (downloadString.indexOf("亿") > 0)
                appInfo.download = (long) (Float.parseFloat(downloadString.replaceAll("亿下载", "")) * 100000000);
            else if (downloadString.indexOf("万") > 0)
                appInfo.download = (long) (Float.parseFloat(downloadString.replaceAll("万下载", "")) * 10000);
            else
                appInfo.download = (long) Float.parseFloat(downloadString.replaceAll("下载", ""));

            //处理安装包大小
            String apkSizeString = page.getHtml().xpath("//div[@class=det-insnum-line]/div[@class=det-size]/text()").replace("M", "").toString();//17.86M
            appInfo.apkSize = (long) Float.parseFloat(apkSizeString.replace("M", "")) * 1024 * 1024;

            //处理评分高低
            String rateString = page.getHtml().xpath("//div[@class=com-blue-star-num]/text()").toString();//3.8分
            appInfo.rating = Float.parseFloat(rateString.replace("分", ""));

            //处理l应用类别
            String typeString = page.getHtml().xpath("//div[@class=det-type-box]/a[@class=det-type-link]/text()").toString();
            appInfo.catoList.add(typeString);

            appInfo.brief = page.getHtml().xpath("//div[@class=det-intro-text]/div[1]/text()").toString();

            //TODO  版本日期目前无法获取,有待改进
            String versionDateString = page.getHtml().xpath("//div[@data-modname=appOthInfo]/div[4]").toString();//显示错误
            appInfo.versionDate = 0;

        }

    }


    private void pageParserForWDJ(Page page, AppInfo appInfo) {

        appInfo.company = page.getHtml().xpath("//dl[@class=infos-list]/dd[7]/a/span/text()").toString();
        if (appInfo.company == null) {
            System.out.println(Thread.currentThread().getId() + " " + appInfo.packageName + "    NOT FIND in WDJ");
            return;
        }
        appInfo.cname = page.getHtml().xpath("//p[@class=app-name]/span[@class=title]/text()").toString();
        appInfo.imgUrl = page.getHtml().xpath("//div[@class=app-icon]/img/@src").toString();

        String versionDateString = page.getHtml().xpath("//dl[@class=infos-list]/dd[4]/time[1]/text()").toString();//201682
        String[] dateArray = versionDateString.split("年|月|日");

        String dateString = "";
        for (String x : dateArray) {
            if (x.length() < 2) {
                dateString += "0" + x;
            } else {
                dateString += x;
            }
        }
        appInfo.versionDate = Integer.parseInt(dateString);
        appInfo.version = page.getHtml().xpath("//dl[@class=infos-list]/dd[5]/text()").toString();
        appInfo.permissionList = new ArrayList<>();
        List<Selectable> pmsNodeList = page.getHtml().xpath("//ul[@class=perms-list]/li").nodes();
        for (Selectable node : pmsNodeList) {
            appInfo.permissionList.add(node.xpath("//li/span/text()").toString());
        }
        appInfo.brief = page.getHtml().xpath("//div[@class=desc-info]/div[@itemprop=description]/text()").toString();
        String apkSizeString = page.getHtml().xpath("//dl[@class=infos-list]/dd[1]/text()").replace("M", "").toString();
        appInfo.apkSize = (long) Float.parseFloat(apkSizeString) * 1024 * 1024;

        String downloadString = page.getHtml().xpath("//span[@class=item]/i[@itemprop=interactionCount]/text()").toString();
        if (downloadString.indexOf("亿") > 0)
            appInfo.download = (long) (Float.parseFloat(downloadString.replaceAll("亿", "")) * 100000000);
        else if (downloadString.indexOf("万") > 0)
            appInfo.download = (long) (Float.parseFloat(downloadString.replaceAll("万", "")) * 10000);
        else
            appInfo.download = (long) Float.parseFloat(downloadString.replaceAll("", ""));//// TODO: 8/25/16 少量人情况字符显示待查询

        List<Selectable> tagNodeList = page.getHtml().xpath("//div[@class=tag-box]").nodes();
        for (Selectable node : tagNodeList) {
            appInfo.catoList.add(node.xpath("//a/text()").toString());
        }

        float favorAmount = Float.parseFloat(page.getHtml().xpath("//div[@class=num-list]/span[2]/i/text()").toString());
        float reviewAmount = Float.parseFloat(page.getHtml().xpath("//div[@class=num-list]/a/i/text()").toString());
        if (reviewAmount != 0)
            appInfo.rating = favorAmount / reviewAmount * 5;
        else
            appInfo.rating = 0;
    }
}


















