import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: Spikerman
 * Mail: mail4spikerman@gmail.com
 * Created Date: 9/12/16
 */


public class ImprovedClient {

    public static void main(String args[]) {

        try {
            while (true) {
                PkgFetch.getInstance().fetchPackage();

                List urlList = linkTransfer(PkgFetch.getInstance().getPackageSet());
//                Set<String> packageSet = new HashSet<>();
//                packageSet.add("com.tencent.qqmusic");
//                List urlList = linkTransfer(packageSet);
                Spider.create(new AppStorePageProcessor(urlList))
                        .addUrl(urlList.get(0).toString())
                        .addPipeline(new AppInfoPipeline())
                        .setDownloader(new DataDownloader())
                        .thread(10)
                        .run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //将 packageSet 内的所有包名转换成其在各应用商店对应的 url 并添加到 appUrlList 中
    static private List linkTransfer(Set<String> packageSet) {
        List<String> appUrlList = new ArrayList<>();
        for (String p : packageSet) {
            appUrlList.add(String.format(AppStorePageProcessor.storeLinkForXIAOMI, p));
            appUrlList.add(String.format(AppStorePageProcessor.storeLinkForWDJ, p));
            appUrlList.add(String.format(AppStorePageProcessor.storeLinkForYYB, p));
        }
        return appUrlList;
    }

}