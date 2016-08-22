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
                appPageLink = String.format(AppStorePageProcessor.storeLinkForMI, packageName);
                break;
            default:
                appPageLink = String.format(AppStorePageProcessor.storeLinkForMI, packageName);
        }

        Spider.create(new AppStorePageProcessor(store, packageName))
                .addUrl(appPageLink)
                .addPipeline(new AppInfoPipeline())
                .setDownloader(new DataDownloader())
                .thread(1)
                .run();
    }
}
