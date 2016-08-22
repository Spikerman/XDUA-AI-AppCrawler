import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenhao on 8/19/16.
 */
public class AppInfo {
    public String packageName;
    public String cname;
    public String company;
    public String imgUrl;
    public long apkSize;
    public String version;
    public int versionDate;
    public List<String> permissionList;
    public int ratingCount;
    public float rating;
    public long download;
    public List<String> catoList = new ArrayList();
    public String brief;

    public AppInfo() {
    }

    public AppInfo(String packageName) {
        this.packageName = packageName;
    }

    public void printAppInfo() {
        System.out.println("包名: " + packageName);
        System.out.println("中文名: " + cname);
        System.out.println("公司: " + company);
        System.out.println("图像链接: " + imgUrl);
        System.out.println("安装包大小: " + apkSize);
        System.out.println("版本号: " + version + " 版本日期: " + versionDate);
        System.out.println("======= 权限 ======");
        permissionList.forEach(
                System.out::println
        );
        System.out.println("===================");
        System.out.println("评分: " + rating + " 评分数量: " + ratingCount);
        System.out.println("======== 简介 ========");
        System.out.println(brief);
        System.out.println("======================");
        System.out.println("下载量: " + download);
        catoList.forEach(
                System.out::println
        );
    }
}
