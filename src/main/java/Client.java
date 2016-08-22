import okhttp3.*;
import org.json.JSONObject;

/**
 * Created by chenhao on 8/19/16.
 */


public class Client {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static OkHttpClient client = new OkHttpClient();

    public static void main(String args[]) {
        Crawler crawler = new Crawler();
        while (true) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("action", "getonepkg");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url("http://api.xdua.org/apps")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONObject responseObj = new JSONObject(response.body().string());
                    if (responseObj.getInt("status") == 0) {
                        String pname = responseObj.getJSONObject("result").get("pname").toString();
                        System.out.println("从服务器获取的Package: " + pname);
                        crawler.setStore("XIAOMI").setPackageName(pname).start();
                    } else {
                        System.out.println("数据库爬取完毕,运行结束");
                        return;
                    }
                } else {
                    System.out.println("与服务器连接失败 " + response + " 系统结束运行");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
