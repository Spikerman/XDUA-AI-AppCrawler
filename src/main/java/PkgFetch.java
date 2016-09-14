import okhttp3.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Spikerman
 * Mail: mail4spikerman@gmail.com
 * Created Date: 9/13/16
 */
public class PkgFetch {
    private static final Set<String> packageSet = new HashSet<>();
    private static final OkHttpClient receiveClient = new OkHttpClient();
    private static PkgFetch instance = null;
    private MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void main(String args[]) {
        try {
            PkgFetch.getInstance().fetchPackage();
            PkgFetch.packageSet.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static PkgFetch getInstance() {
        if (instance == null) {
            synchronized (Crawler.class) {
                instance = new PkgFetch();
            }
        }
        return instance;
    }

    public Set<String> getPackageSet() {
        return packageSet;
    }

    public void fetchPackage() throws Exception {
        packageSet.clear();
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new RequestTask());
            thread.start();
            threadList.add(thread);
        }

        for (Thread thread : threadList) {
            thread.join();
        }
        packageSet.forEach(p -> System.out.println(p + " START"));
    }

    private class RequestTask implements Runnable {
        @Override
        public void run() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", "getonepkg");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url("http://api.xdua.org/apps")
                        .post(body)
                        .build();
                Response response = receiveClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONObject responseObj = new JSONObject(response.body().string());
                    String packageName = responseObj.getJSONObject("result").get("pname").toString();
                    packageSet.add(packageName);
                } else {
                    System.out.println("XDUA Server Retrieve Fail " + response);
                }
                response.body().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
