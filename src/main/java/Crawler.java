import us.codecraft.webmagic.Spider;

/**
 * Created by chenhao on 8/19/16.
 */
public class Crawler {
    public String packageName;
    public String store;

    public Crawler(String packageName, String store) {
        this.packageName = packageName;
        this.store = store;
    }

    public Crawler(String packageName) {
        this.packageName = packageName;
        store = "XIAOMI";
    }

    public Crawler() {
        store = "XIAOMI";
    }

    public static void main(String args[]) {
        Crawler crawler = new Crawler();
        String qq = "com.tencent.qqmusic";
        String xiaomiStore = "XIAOMI";

        crawler.setPackageName(qq).setStore(xiaomiStore).start();

    }

    public Crawler setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public Crawler setStore(String store) {
        this.store = store;
        return this;
    }

    public void start() {
        String appPageLink;
        switch (store) {
            case "XIAOMI":
                appPageLink = String.format(AppStorePageProcessor.storeLinkForXIAOMI, packageName);
                break;
            case "YYB":
                appPageLink = String.format(AppStorePageProcessor.storeLinkForYYB, packageName);
                break;
            default:
                appPageLink = String.format(AppStorePageProcessor.storeLinkForXIAOMI, packageName);
        }

        Spider.create(new AppStorePageProcessor(store, packageName))
                .addUrl(appPageLink)
                .addPipeline(new AppInfoPipeline())
                .setDownloader(new DataDownloader(packageName))
                .thread(1)
                .run();
    }
}
