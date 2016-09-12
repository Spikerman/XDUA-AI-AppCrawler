import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Author: Spikerman
 * Mail: mail4spikerman@gmail.com
 * GitHub: https://github.com/Spikerman
 * Created Date: 9/12/16
 */

public class AsycClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final OkHttpClient receiveClient = new OkHttpClient();

    public static void main(String args[]) {
        while (true) {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("action", "getonepkg");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url("http://api.xdua.org/apps")
                        .post(body)
                        .build();

                receiveClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println(" XDUA Server Retrieve Fail ");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseObj = new JSONObject(response.body().string());
                                String packageName = responseObj.getJSONObject("result").get("pname").toString();
                                Crawler.getInstance()
                                        .addStore("YYB")
                                        .addStore("WDJ")
                                        .setPackage(packageName)
                                        .start();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
