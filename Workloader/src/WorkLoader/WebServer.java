package WorkLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class WebServer {	
	private CloseableHttpClient httpclient;
	private String address;
	
	WebServer(String add) {
		this.httpclient = HttpClients.createDefault();
		this.address = "http://b"+add+".seng.uvic.ca:44451/WebServer_"+add+"/WebServlet";
	}

	public void getMsg() {
		HttpGet httpget = new HttpGet(address);
		try {
			httpclient.execute(httpget); // Discard form from server.
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMsg(String value) {	
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("param0", value));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		HttpPost httppost = new HttpPost(address);
		httppost.setEntity(entity);
		try {
			CloseableHttpResponse response = httpclient.execute(httppost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		    String line = "";
		    while ((line = rd.readLine()) != null) {
		    	System.out.println("WEB SERVER RECEIVED: " + line);
		    }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
