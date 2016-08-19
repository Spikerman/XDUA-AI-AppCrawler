/**
 * Created by chenhao on 8/19/16.
 */
public class Client {
    public static void main(String args[]) {
        Crawler crawler = new Crawler();
        crawler.setPackageName("com.meitu.meipaimv").setStore("XIAOMI").start();
    }
}
