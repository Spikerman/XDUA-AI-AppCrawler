import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chenhao on 8/19/16.
 */
public class Crawler {
    private String packageName;
    private Set<String> storeSet = new HashSet<>();

    public static void main(String args[]) {
        Crawler crawler = new Crawler();
        String qq = "com.tencent.qqmusic";

        String xiaomi = "XIAOMI";
        String yyb = "YYB";

        crawler.setPackage(qq).addStore(yyb).start();

    }

    public Crawler setPackage(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public Crawler addStore(String store) {
        this.storeSet.add(store);
        return this;
    }

    public void start() {
        List<Thread> threadList = new ArrayList<>();
        for (String store : storeSet) {
            Runnable runnable = new CrawlTask(store, packageName);
            Thread thread = new Thread(runnable);
            thread.start();
            threadList.add(thread);
        }

        try {
            for (Thread thread : threadList) {
                thread.join();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class CrawlTask implements Runnable {
        String store;
        String packageName;

        public CrawlTask(String store, String packageName) {
            this.packageName = packageName;
            this.store = store;
        }

        @Override
        public void run() {
            String appPageLink = "";
            switch (store) {
                case "XIAOMI":
                    appPageLink = String.format(AppStorePageProcessor.storeLinkForXIAOMI, packageName);
                    break;
                case "YYB":
                    appPageLink = String.format(AppStorePageProcessor.storeLinkForYYB, packageName);
                    break;
                default: {
                    System.out.println(store + " STORE NOT EXIST");
                    return;
                }
            }

            Spider.create(new AppStorePageProcessor(store, packageName))
                    .addUrl(appPageLink)
                    .addPipeline(new AppInfoPipeline())
                    .setDownloader(new DataDownloader(packageName, store))
                    .thread(1)
                    .run();
        }
    }
}
