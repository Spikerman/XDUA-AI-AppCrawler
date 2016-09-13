import okhttp3.*;
import org.json.JSONObject;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;

/**
 * Created by chenhao on 8/19/16.
 */
public class AppInfoPipeline implements Pipeline {

    private static final OkHttpClient postClient = new OkHttpClient();

    @Override
    public void process(ResultItems resultItems, Task task) {
        AppInfo appInfo = resultItems.get("appinfo");
        String store = resultItems.get("storeList");
        String appStore;
        switch (store) {
            case "XIAOMI":
                appStore = "app.xiaomi.com";
                break;
            case "YYB": {
                appStore = "sj.qq.com";
                if (resultItems.get("ratingCount") != null)
                    appInfo.ratingCount = resultItems.get("ratingCount");
            }
            break;
            case "WDJ":
                appStore = "www.wandoujia.com";
                break;
            default:
                appStore = "app.xiaomi.com";

        }
        //检查是否成功从网站获取到APP信息,若中文名为空,则代表获取失败,返回
        if (appInfo.cname != null && resultItems.get("ratingCount") != null) {
            JSONObject jsonObject = new JSONObject();
            RequestBody requestBody;
            try {
                jsonObject.put("action", "setinfo");
                jsonObject.put("isfrom", appStore);
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

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                requestBody = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url("http://api.xdua.org/apps")
                        .post(requestBody)
                        .build();

                //todo:改为串型
                postClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            System.out.println(Thread.currentThread().getId() + " " + appInfo.packageName + "   SUCCESS " + store);
                        } else {
                            System.out.println("XDUA Server Upload Error " + response);
                        }
                        response.body().close();
                    }
                });

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}
