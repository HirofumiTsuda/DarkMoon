package com.dacho.darkmoon.async;

import android.os.AsyncTask;
import android.util.Log;

import com.dacho.darkmoon.values.TwitterKey;

import org.apache.commons.lang.RandomStringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import twitter4j.BASE64Encoder;

/**
 * Created by hirofumi on 15/03/02.
 */
public class OauthAccessToken extends AsyncTask<Void,Void,String> implements TwitterKey {

    private String REQEST_TOKEN;
    private String REQEST_SECRET;
    private String OAUTH_VERIFIER;
    private String OAUTH_TOKEN;
    private String requestStr = "https://api.twitter.com/oauth/access_token";


    public OauthAccessToken(String REQEST_TOKEN,String REQEST_SECRET,String OAUTH_VERIFIER,String OAUTH_TOKEN){
        this.REQEST_TOKEN = REQEST_TOKEN;
        this.REQEST_SECRET = REQEST_SECRET;
        this.OAUTH_VERIFIER = OAUTH_VERIFIER;
        this.OAUTH_TOKEN = OAUTH_TOKEN;
    }

    @Override
    protected String doInBackground(Void... par) {
        //リクエストとほぼおなじ

        HttpsURLConnection connection = null;
        try {
            //値をつなげる
            //まず値をいれる
            String OAUTH_NONCE = RandomStringUtils.randomAlphanumeric(20);
            String TIMESTAMP = String.valueOf(getUnixTime());
            //値を入手したのでmapに投げる
            SortedMap<String, String> params = new TreeMap<String, String>();
            params.put("oauth_consumer_key", CONSUMER_KEY);
            params.put("oauth_token", OAUTH_TOKEN);
            params.put("oauth_signature_method", "HMAC-SHA1");
            params.put("oauth_timestamp", TIMESTAMP);
            params.put("oauth_nonce", OAUTH_NONCE);
            params.put("oauth_verifier", OAUTH_VERIFIER);
            params.put("oauth_version", "1.0");
            //パラメータを連結
            String paramStr = "";
            for (Map.Entry<String, String> param : params.entrySet()) {
                paramStr += "&" + param.getKey() + "=" + urlEncode(param.getValue());
            }
            //最初に&がはいているので除去
            paramStr = paramStr.substring(1);
            //署名対象となるテキストを作成
            String text = "POST" + "&" + urlEncode(requestStr) + "&" + urlEncode(paramStr);
            Log.d("text", text);
            //署名キーの作成
            String key = urlEncode(CONSUMER_SECRET) + "&" + urlEncode(REQEST_SECRET);
            //HAMC-SHA1で署名を生成
            SecretKeySpec singningKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance(singningKey.getAlgorithm());
            mac.init(singningKey);
            byte[] rawHmac = mac.doFinal(text.getBytes());
            String signature = BASE64Encoder.encode(rawHmac);
            //署名ができたのでmapに追加
            params.put("oauth_signature", signature);
            //Authorizationヘッダの作成 Oauthのパラメータをいれる
            String header = "";
            for (Map.Entry<String, String> param : params.entrySet()) {
                header += ", " + param.getKey() + "=\""
                        + urlEncode(param.getValue()) + "\"";
            }
            //はじめの","は削除
            header = header.substring(2);
            //先頭に"OAuth "
            String authorizationHeader = "OAuth " + header;
            //APIにアクセス
            URL url = new URL(requestStr);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            //connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", authorizationHeader);
            Log.d("header", connection.getRequestProperty("Authorization"));
            Log.d("call", "end properties");
            connection.connect();
            Log.d("call", "connect");
            //返り値を読み込む
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Log.d("call", "set inputstream");
            String response;
            response = br.readLine();
            Log.d("request token", response);
            //結果をそのままかえす
            return response;
        }catch (FileNotFoundException fne){
            fne.printStackTrace();
            try {
                Log.d("error", connection.getResponseMessage());
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }catch (MalformedURLException mue){
            mue.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (RuntimeException e){
            e.printStackTrace();
        }catch (NoSuchAlgorithmException nae){
            nae.printStackTrace();
        }catch (InvalidKeyException ike){
            ike.printStackTrace();
        }

        return null;
    }


    private String urlEncode(String string){
        try{
            return URLEncoder.encode(string, "UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }


    private int getUnixTime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }
}
