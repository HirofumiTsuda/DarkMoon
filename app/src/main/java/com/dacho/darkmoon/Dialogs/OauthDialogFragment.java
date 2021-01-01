package com.dacho.darkmoon.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
 * Created by hirofumi on 15/02/27.
 */
public class OauthDialogFragment extends DialogFragment implements TwitterKey {

    private String scheme = "darkmoontwitter://main";
    private String requestStr = "https://api.twitter.com/oauth/request_token";
    private static String REQUEST_TOKEN = "";
    private static String REQUEST_TOKEN_SECRET = "";
    private Activity activity;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Oauth認証をするかどうかをだす
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Oauth認証を行います\n認証しますか？")
                .setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //認証よびだし
                        new OauthAsyncTask().execute();
                        return;
                    }
                })
                .setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //とくになし
                        return;
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public static String getREQUEST_TOKEN(){
        return REQUEST_TOKEN;
    }

    public static String getREQUEST_TOKEN_SECRET(){
        return REQUEST_TOKEN_SECRET;
    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }

    public void setScheme(String scheme){
        this.scheme = scheme;
    }

    //内部クラスとして、通信を行うクラスをもたせる

    class OauthAsyncTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            return startOauth();
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null)
                return;

            Uri uri = Uri.parse("https://api.twitter.com/oauth/authorize?oauth_token="+result);
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            activity.startActivity(i);
            activity.finish();
        }
    }



    private String startOauth(){
        //まずリクエストトークンを取得
        HttpsURLConnection connection = null;
        try {
            //値をつなげる
            //まず値をいれる
            String OAUTH_NONCE = RandomStringUtils.randomAlphanumeric(20);
            String TIMESTAMP = String.valueOf(getUnixTime());
            String REQUEST_TOKEN = "";
            String REQUEST_TOKEN_SECRET = "";
            //値を入手したのでmapに投げる
            SortedMap<String, String> params = new TreeMap<String, String>();
            params.put("oauth_callback",CALLBACK);
            params.put("oauth_consumer_key", CONSUMER_KEY);
            params.put("oauth_signature_method", "HMAC-SHA1");
            params.put("oauth_timestamp", TIMESTAMP);
            params.put("oauth_nonce", OAUTH_NONCE);
            params.put("oauth_version", "1.0");
            //パラメータを連結
            String paramStr = "";//"oauth_callback=" + scheme;
            for (Map.Entry<String, String> param : params.entrySet()) {
                paramStr += "&" + param.getKey() + "=" + urlEncode(param.getValue());
            }
            //最初に&がはいているので除去
            paramStr = paramStr.substring(1);
            //署名対象となるテキストを作成
            String text = "POST" + "&" + urlEncode(requestStr) + "&" + urlEncode(paramStr);
            Log.d("text", text);
            //署名キーの作成
            String key = urlEncode(CONSUMER_SECRET) + "&" + urlEncode("");
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
            //かえってきたので、ここかtokenをぬきだす
            // & で分割
            // = で分割して　うしろ側をとる
            String request_token = response.split("&")[0].split("=")[1];
            String request_secret = response.split("&")[1].split("=")[1];
            setToken(request_token,request_secret);
            Log.d("REQUEST_TOKEN",request_token);
            Log.d("SECRET",request_secret);
            return request_token;
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
            return URLEncoder.encode(string,"UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    private void setToken(String request_token,String request_secret){
        REQUEST_TOKEN = request_token;
        REQUEST_TOKEN_SECRET = request_secret;
    }

    private int getUnixTime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }


}
