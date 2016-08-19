import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * Created by chenhao on 8/19/16.
 */
public class AppInfoPipeline implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {
        AppInfo appInfo = resultItems.get("appinfo");
        appInfo.printAppInfo();
    }
}
