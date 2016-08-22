import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenhao on 8/19/16.
 */
public class DataDownloader extends AbstractDownloader {
    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();
    private Logger logger = LoggerFactory.getLogger(getClass());
    private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

    private CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            return httpClientGenerator.getClient(null);
        }
        String domain = site.getDomain();
        CloseableHttpClient httpClient = httpClients.get(domain);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(domain);
                if (httpClient == null) {
                    httpClient = httpClientGenerator.getClient(site);
                    httpClients.put(domain, httpClient);
                }
            }
        }
        return httpClient;
    }

    @Override
    public Page download(Request request, Task task) {
        Site site = null;
        if (task != null) {
            site = task.getSite();
        }
        Set<Integer> acceptStatCode;
        String charset = null;
        Map<String, String> headers = null;
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            charset = site.getCharset();
            headers = site.getHeaders();
        } else {
            acceptStatCode = Sets.newHashSet(200);
        }

        CloseableHttpResponse httpResponse = null;
        try {
            HttpUriRequest httpUriRequest = getHttpUriRequest(request, site, headers);
            CloseableHttpClient httpClient = getHttpClient(site);
            httpResponse = httpClient.execute(httpUriRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusAccept(acceptStatCode, statusCode)) {
                if (charset == null) {
                    String value = httpResponse.getEntity().getContentType().getValue();
                    charset = UrlUtils.getCharset(value);
                }
                Page page = handleResponse(request, charset, httpResponse, task);
                onSuccess(request);
                return page;
            } else {
                logger.warn("code error " + statusCode + "\t" + request.getUrl());
                if (statusCode == 403) {
                    return addToCycleRetry(request, site);
                } else {
                    logger.info(request.getUrl() + " has been added to the failUrlList");
                    logger.info("add to cycle retry");
                    return addToCycleRetry(request, site);
                }
            }

        } catch (IOException e) {
            logger.warn("download page " + request.getUrl() + " error");
            if (site.getCycleRetryTimes() > 0) {
                logger.info("放入队列重连");
                return addToCycleRetry(request, site);
            }
            onError(request);
            return null;
        } finally {
            try {
                if (httpResponse != null) {
                    //ensure the connection is released back to pool
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
                logger.warn("close response fail", e);
            }
        }
    }

    @Override
    public void setThread(int thread) {
        httpClientGenerator.setPoolSize(thread);
    }

    protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
        return acceptStatCode.contains(statusCode);
    }

    protected HttpUriRequest getHttpUriRequest(Request request, Site site, Map<String, String> headers) {
        RequestBuilder requestBuilder = selectRequestMethod(request).setUri(request.getUrl());
        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectionRequestTimeout(site.getTimeOut())
                .setSocketTimeout(site.getTimeOut())
                .setConnectTimeout(site.getTimeOut())
                .setCookieSpec(CookieSpecs.BEST_MATCH);
        if (site != null && site.getHttpProxy() != null) {
            requestConfigBuilder.setProxy(site.getHttpProxy());
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        return requestBuilder.build();
    }

    protected RequestBuilder selectRequestMethod(Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            //default get
            return RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            RequestBuilder requestBuilder = RequestBuilder.post();
            NameValuePair[] nameValuePair = (NameValuePair[]) request.getExtra("nameValuePair");
            if (nameValuePair.length > 0) {
                requestBuilder.addParameters(nameValuePair);
            }
            return requestBuilder;
        } else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
            return RequestBuilder.head();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
            return RequestBuilder.put();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
            return RequestBuilder.delete();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
            return RequestBuilder.trace();
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        String content = IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        Page page = new Page();
        page.setRawText(content);
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        return page;
    }
}
