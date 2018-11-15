package com.live.demo.Util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Copyright  2018 eSunny Info. Tianchi Ltd. All rights reserved.
 *
 * @Package: com.zb
 * @author: WangZhichao
 * @date: 2018/11/13 12:58
 * @Description:
 */

public class Util {

    public static String syncRequest(String appServerUrl) {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(appServerUrl).openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(10000);
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int length = httpConn.getContentLength();
            if (length <= 0) {
                length = 16 * 1024;
            }
            InputStream is = httpConn.getInputStream();
            byte[] data = new byte[length];
            int read = is.read(data);
            is.close();
            if (read <= 0) {
                return null;
            }
            return new String(data, 0, read);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
