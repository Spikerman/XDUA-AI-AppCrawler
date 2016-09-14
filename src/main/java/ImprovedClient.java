import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: Spikerman
 * Mail: mail4spikerman@gmail.com
 * Created Date: 9/12/16
 */


public class ImprovedClient {

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
            PkgFetch.getInstance().fetchPackage();
            List urlList = linkTransfer(PkgFetch.getInstance().getPackageSet());
            Spider.create(new AppStorePageProcessor(urlList))
                    .addUrl(urlList.get(0).toString())
                    .addPipeline(new AppInfoPipeline())
                    .setDownloader(new DataDownloader())
                    .thread(10)
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将 packageSet 内的所有包名转换成其在各应用商店对应的 url 并添加到 appUrlList 中
    static private List linkTransfer(Set<String> packageSet) {
        List<String> appUrlList = new ArrayList<>();
        for (String p : packageSet) {
            appUrlList.add(String.format(AppStorePageProcessor.storeLinkForXIAOMI, p));
            appUrlList.add(String.format(AppStorePageProcessor.storeLinkForWDJ, p));
            appUrlList.add(String.format(AppStorePageProcessor.storeLinkForYYB, p));
        }
        return appUrlList;
    }

}