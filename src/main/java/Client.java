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
                        crawler.setStore("XIAOMI").setPackageName(pname).start();
                        System.out.println();
                    } else {
                        System.out.println("Crawler Finish");
                        return;
                    }
                } else {
                    System.out.println("XDUA Server Retrieve Fail " + response);
                    return;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }


}
