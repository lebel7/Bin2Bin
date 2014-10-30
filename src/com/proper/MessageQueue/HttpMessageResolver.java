package com.proper.MessageQueue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.proper.Logger.LogHelper;
import com.proper.bin2bin.R;
import com.proper.data.Contact;
import com.proper.data.LogEntry;
import com.proper.data.core.IHttpMessageResolver;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by Lebel on 16/05/2014.
 */
public class HttpMessageResolver implements IHttpMessageResolver {
    private String deviceIMEI = "";
    private static final String ApplicationID = "BinMove";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private LogHelper logger = new LogHelper();
    private String response;
    private List<Contact> contactList;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDefaultConfig() {
        return setConfig(R.integer.CONFIG_LIVESERVER);
    }

    public String setConfig(int configurqation) {
        String newConfig = "";
        switch(configurqation) {
            case R.integer.CONFIG_TESTSERVER:
                newConfig = "http://192.168.10.248:9090/warehouse.support/api/v1/message/barcodequery2";
                break;
            case R.integer.CONFIG_LIVESERVER:
                newConfig = "http://192.168.10.246:9090/warehouse.support/api/v1/message/barcodequery2";
                break;
        }
        return newConfig;
    }

    @Override
    public String resolveMessageQuery(Message msg) {

        try {
            URL url = new URL(getDefaultConfig());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false); // new line
            conn.setDoInput(true); //new line
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(input);
            osw.flush();
            osw.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            //Serialise the returned entity
            //LogEntry newEntry = mapper.readValue(conn.getInputStream(), LogEntry.class);
            //Or Get the string returned
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response string
                sb.append(line + "");
            }

            reader.close(); // new line added !
            // Append Server Response To Content String but do nothing with it - for now...
            setResponse(sb.toString().trim());

            conn.disconnect();
        } catch(MalformedURLException ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQuery", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.Log(log);
        } catch (IOException ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQuery", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.Log(log);
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQuery", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.Log(log);
        }
        return response;
    }

    public String resolveMessageQueue(Message msg) {
        String responseBody = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);
            DefaultHttpClient http = new DefaultHttpClient();
            HttpPost httpMethod = new HttpPost();
            httpMethod.setURI(new URI(getDefaultConfig()));
            httpMethod.setHeader("Accept", "application/json");
            httpMethod.setHeader("Content-type", "application/json");
            httpMethod.setEntity(new StringEntity(input));
            HttpResponse response = http.execute(httpMethod);
            int responseCode = response.getStatusLine().getStatusCode();
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    HttpEntity entity = response.getEntity();
                    if(entity != null)
                    {
                        responseBody = EntityUtils.toString(entity);
                    }
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQueue", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.Log(log);
        }
        return responseBody;
    }

    public String resolveMessageQueueAsync(Message msg) {
        String responseBody = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(msg);
            DefaultHttpClient http = new DefaultHttpClient();
            HttpPost httpMethod = new HttpPost();
            httpMethod.setURI(new URI(getDefaultConfig()));
            httpMethod.setHeader("Accept", "application/json");
            httpMethod.setHeader("Content-type", "application/json");
            httpMethod.setEntity(new StringEntity(input));
            HttpResponse response = http.execute(httpMethod);
            int responseCode = response.getStatusLine().getStatusCode();
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    HttpEntity entity = response.getEntity();
                    if(entity != null)
                    {
                        responseBody = EntityUtils.toString(entity);
                    }
                    break;
            }

            Client client = ClientBuilder.newClient();
            final Future<Response> responseFuture = client.target("http://192.168.10.248:9080").path("/api/v1/message/barcodequery2").request().async().get(new InvocationCallback<Response>() {
                //String responseBody1 = "";
                @Override
                public void completed(Response response) {
                    Log.w("resolveMessageQueueAsync - ", "Response status code" + response.getStatus() + "received");
                    System.out.println("Response status code "
                            + response.getStatus() + " received.");
                }

                @Override
                public void failed(Throwable throwable) {
                    Log.w("resolveMessageQueueAsync - ", "Invocation failed !");
                    System.out.println("Invocation failed.");
                    throwable.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveMessageQueue", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.Log(log);
        }
        return responseBody;
    }

    @Override
    public String resolveMessageAction(Message msg) {
        return null;
    }

    @Override
    public List<Contact> resolveContacts(Context context) {
        //Code runs
        String dirName = context.getApplicationInfo().dataDir + "/contacts/";
        String fileFullName = context.getApplicationInfo().dataDir + "/contacts/contact.json";
        File fileDir = new File(dirName);
        File jsonContact = new File(fileFullName);
        //Check DIR
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        ObjectMapper mapper = new ObjectMapper();
        if (jsonContact.exists()) {
            try {
                //List<Contact> contactList = mapper.readValue(jsonContact, mapper.getTypeFactory().constructCollectionType(List.class, Contact.class));
                contactList = mapper.readValue(jsonContact, new TypeReference<List<Contact>>(){});
            } catch (IOException e) {
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            }
        }else {
            //Get contacts from a rest web service
            try {
                //URL url = new URL("http://192.168.10.248:9080/warehouse.support/api/v1/contact/getall");
                URL url = new URL("http://192.168.10.246:9080/warehouse.support/api/v1/contact/getall");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                ObjectMapper map = new ObjectMapper();
                contactList = map.readValue(conn.getInputStream(), new TypeReference<List<Contact>>(){});

                conn.disconnect();
                mapper.writeValue(new File(context.getApplicationInfo().dataDir + "/contacts/contact.json"), contactList);
            } catch (IOException e) {
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveContacts", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            } catch(Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - resolveContacts", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                logger.Log(log);
            }
        }
        return contactList;
    }

    public boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK);
            } catch (IOException e) {
                Log.e("LOG_TAG", "Error checking internet connection", e);
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "HttpMessageResolver - hasActiveInternetConnection", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.Log(log);
            }
        } else {
            Log.d("LOG_TAG", "No network available!");
        }
        return false;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
