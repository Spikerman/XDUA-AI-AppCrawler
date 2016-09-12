import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chenhao on 8/19/16.
 */
public class Crawler {
    private static Crawler instance = null;
    private String packageName;
    private Set<String> storeSet = new HashSet<>();

    public static Crawler getInstance() {
        if (instance == null) {
            synchronized (Crawler.class) {
                instance = new Crawler();
            }
        }
        return instance;
    }


    public static void main(String args[]) {
        String qq = "com.tencent.qqmusic";
        String xiaomi = "XIAOMI";
        String yyb = "YYB";
        String wdj = "WDJ";
        Crawler.getInstance().setPackage(qq).addStore(wdj).start();
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
                case "WDJ":
                    appPageLink = String.format(AppStorePageProcessor.storeLinkForWDJ, packageName);
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
