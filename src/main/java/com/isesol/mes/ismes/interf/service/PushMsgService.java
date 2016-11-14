package com.isesol.mes.ismes.interf.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;

import net.sf.json.JSONObject;

public class PushMsgService {
//	@SuppressWarnings("unchecked")
//	public String pushMsg2NC(Parameters parameters, Bundle bundle) {
//		CloseableHttpClient httpclient = HttpClients.createDefault();
//		HttpPost httpPost = new HttpPost("http://10.24.10.249:8333/demo/customPostJson1");
//		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//		nvps.add(new BasicNameValuePair("machineNo", "vip"));
//		nvps.add(new BasicNameValuePair("type", "3462"));
//		nvps.add(new BasicNameValuePair("content",
//				"{\"sys_time\":\"1\",\"cfg_MachineId\":\"1\",\"ncprogramm\":\"1\",\"N\":\"1\"}"));
//		HttpResponse rs;
//		try {
//			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
//			rs = httpclient.execute(httpPost);
//			BufferedReader br = new BufferedReader(new InputStreamReader(rs.getEntity().getContent()));
//			String line = "";
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//			}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	@SuppressWarnings("unchecked")
	public String pushMsg2NC(Parameters parameters, Bundle bundle) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://223.223.1.19/agentServer/order/jsonOrder");
//		HttpPost httpPost = new HttpPost("http://223.223.1.14:8080/agentServer/order/jsonOrder");
//		HttpPost httpPost = new HttpPost("http://10.24.11.248/agentServer/order/jsonOrder");
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//		String destination = parameters.getString("destination");
		String sbbh  =  parameters.getString("sbbh");//"A131420089";
//		nvps.add(new BasicNameValuePair("destination", destination));
		nvps.add(new BasicNameValuePair("destination", "SMTCL_MACHINE_A131420097"));
//		nvps.add(new BasicNameValuePair("destination", sbbh));//A131420097
		nvps.add(new BasicNameValuePair("switchs", "on"));
		nvps.add(new BasicNameValuePair("frequence", "0"));
		nvps.add(new BasicNameValuePair("level", "0"));
		nvps.add(new BasicNameValuePair("orderType", "92"));
		nvps.add(new BasicNameValuePair("cmdId", ""));
		String orderData = parameters.getString("orderData"); 
		nvps.add(new BasicNameValuePair("orderData",orderData));
//		nvps.add(new BasicNameValuePair("orderData",
//				"{\"content\":\"\",\"md5\":\"be213c6329e4d56f33fd9079693e7d72\",\"downloadUrl\":\"http://10.24.11.246:8333/ismes-web/pm/nc/downloadFiles?wjid=8247\",\"id\":\"\",\"msgType\":\"1\"}"));
		HttpResponse rs;
		boolean success = false;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
			httpPost.setConfig(requestConfig);
			rs = httpclient.execute(httpPost);
			BufferedReader br = new BufferedReader(new InputStreamReader(rs.getEntity().getContent()));
			String line = "";
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				JSONObject jsonObject = JSONObject.fromObject(line);  
				success = (Boolean) jsonObject.get("success");
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			bundle.setError(e.getMessage());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			bundle.setError(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			bundle.setError(e.getMessage());
		}finally{
			bundle.put("success", success);
		}
		return null;
	}
}
