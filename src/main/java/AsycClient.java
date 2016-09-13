import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Spikerman
 * Mail: mail4spikerman@gmail.com
 * Created Date: 9/12/16
 */


public class AsycClient {

    public static void main(String args[]) {

        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("action", "getonepkg");
//            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
//            Request request = new Request.Builder()
//                    .url("http://api.xdua.org/apps")
//                    .post(body)
//                    .build();
//
//
//            receiveClient.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    System.out.println(" XDUA Server Retrieve Fail ");
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        try {
//                            JSONObject responseObj = new JSONObject(response.body().string());
//                            String packageName = responseObj.getJSONObject("result").get("pname").toString();
////                                Crawler.getInstance()
////                                        .addStore("YYB")
////                                        .addStore("WDJ")
////                                        .addStore("XIAOMI")
////                                        .setPackage(packageName)
////                                        .start();
//                            pnameSet.add(packageName);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        } finally {
//                            response.body().close();
//                        }
//                    }
//                }
//            });
            PkgFetch.getInstance().startFetch();
            ExecutorService service = Executors.newFixedThreadPool(5);
            for (String name : PkgFetch.getInstance().getPackageSet()) {
                System.out.println(name + "START");
                CrawlerTask task = new CrawlerTask(name);
                service.execute(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}