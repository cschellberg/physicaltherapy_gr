package com.agileapps.pt.util;

import com.agileapps.pt.pojos.StatusAndResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class HttpUtils {

    public static StatusAndResponse getHttpResponse(String urlStr) throws Exception, IOException {
        URL url = new URL(urlStr);
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            int responseCode = httpConnection.getResponseCode();
            Scanner scanner = new Scanner(httpConnection.getInputStream());
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine().trim()).append("\n");
                ;
            }
            return (new StatusAndResponse(responseCode, stringBuilder.toString().trim()));

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
