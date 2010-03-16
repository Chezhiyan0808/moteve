package com.moteve.mca;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ConnectionUtils {
    
    public static String receiveResponse(HttpURLConnection conn) throws IOException {
	conn.setConnectTimeout(10000);
	conn.setReadTimeout(10000);
	// retrieve the response from server
	InputStream is = null;
	try {
		is = conn.getInputStream();
		int ch;
		StringBuffer sb = new StringBuffer();
		while ((ch = is.read()) != -1) {
			sb.append((char) ch);
		}
		return sb.toString();
	} catch (IOException e) {
		throw e;
	} finally {
		if (is != null) {
			is.close();
		}
	}
    }

}
