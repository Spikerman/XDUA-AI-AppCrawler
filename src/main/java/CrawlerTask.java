/**
 * Author: Spikerman
 * Mail: mail4spikerman@gmail.com
 * Created Date: 9/13/16
 */
class CrawlerTask implements Runnable {
    private String packageName;

    public CrawlerTask(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void run() {
        Crawler.getInstance()
                .addStore("YYB")
                .addStore("WDJ")
                .addStore("XIAOMI")
                .setPackage(packageName)
                .start();
    }
}
