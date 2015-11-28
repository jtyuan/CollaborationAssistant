package edu.pku.assistant.Tool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class CustomerHttpClient {

	public static final String CHARSET = HTTP.UTF_8;
	public static HttpClient customerHttpClient;
	private static final String TAG = "CustomerHttpClient";
	public CustomerHttpClient() {
		// TODO Auto-generated constructor stub
		getHttpClient();
	}
	public static synchronized HttpClient getHttpClient(){
		if(customerHttpClient == null){
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			HttpProtocolParams.setUseExpectContinue(params,true);
			HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
                    + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			//超时设置
			//从连接池中取连接的超时时间
			ConnManagerParams.setTimeout(params, 100000);
			//连接超时
			HttpConnectionParams.setConnectionTimeout(params, 200000);
			//请求超时
			HttpConnectionParams.setSoTimeout(params, 400000);
			//设置我们的HttpClient支持http和https两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http",PlainSocketFactory.getSocketFactory(),80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			//使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,schReg);
			customerHttpClient  = new DefaultHttpClient(conMgr,params);		
		}
		return customerHttpClient;
	}
	public static String post(String url, List<NameValuePair> formparams) {
        try {
            // 编码参数
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
                    CHARSET);
            // 创建POST请求
            HttpPost request = new HttpPost(url);
            request.setEntity(entity);
            // 发送请求
            HttpClient client = getHttpClient();
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            	throw new RuntimeException("请求失败");
            }
            HttpEntity resEntity =  response.getEntity();
            String result = (resEntity == null) ? null : EntityUtils.toString(resEntity, CHARSET);
            JSONObject tmp = null;
            try {
				tmp = new JSONObject(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            result = tmp.toString();
            return result;
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, e.getMessage());
            return null;
        } catch (ClientProtocolException e) {
            Log.w(TAG, e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException("连接失败", e);
        }
    }
	
	public static String get(String url) throws ParseException, IOException{
		HttpGet request = new HttpGet(url);
		HttpClient client = getHttpClient();
		HttpResponse response;
		HttpEntity resEntity = null;
		
		try {
			response = client.execute(request);
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	            throw new RuntimeException("请求失败");
	        }
			resEntity =  response.getEntity();
	        	
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (resEntity == null) ? null : EntityUtils.toString(resEntity, CHARSET);
		
	}
}
