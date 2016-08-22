import okhttp3.*;
import org.json.JSONObject;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * Created by chenhao on 8/19/16.
 */
public class AppInfoPipeline implements Pipeline {
    public MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public OkHttpClient client = new OkHttpClient();
    Crawler crawler = new Crawler();

    @Override
    public void process(ResultItems resultItems, Task task) {
        AppInfo appInfo = resultItems.get("appinfo");
        //检查是否成功从网站获取到APP信息,若中文名为空,则代表获取失败,返回
        if (appInfo.cname != null) {
            System.out.println(appInfo.packageName + " 获取成功");
            appInfo.printAppInfo();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("action", "setinfo");
                jsonObject.put("isfrom", "app.xiaomi.com");
                jsonObject.put("pname", appInfo.packageName);
                jsonObject.put("rating", appInfo.rating);
                jsonObject.put("catos", appInfo.catoList);
                jsonObject.put("com", appInfo.company);
                jsonObject.put("name", appInfo.cname);
                jsonObject.put("brief", appInfo.brief);
                jsonObject.put("icon", appInfo.imgUrl);
                jsonObject.put("pms", appInfo.permissionList);
                jsonObject.put("version", appInfo.version);
                jsonObject.put("udate", appInfo.versionDate);
                jsonObject.put("ratingc", appInfo.ratingCount);
                jsonObject.put("downloadc", appInfo.download);
                jsonObject.put("apksize", appInfo.apkSize);

                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url("http://api.xdua.org/apps")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    System.out.println(response.body().string());
//                    JSONObject responseObj = new JSONObject(response.body().string());
//                    if (responseObj.getInt("status") == 0) {
//                        String pname = responseObj.getJSONObject("result").get("pname").toString();
//                        System.out.println("从服务器获取的Package: " + pname);
//                        crawler.setStore("XIAOMI").setPackageName(pname).start();
//                    } else {
//                        System.out.println("爬去完毕,运行结束");
//                        return;
//                    }
                } else {
                    System.out.println("Unexpected code " + response + " 与服务器连接失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}
