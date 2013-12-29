import java.io.*;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * This example demonstrates a basic asynchronous HTTP request / response exchange.
 * Response content is buffered in memory for simplicity.
 */
public class AsyncClientHttpExchange {

    public static void main(final String[] args) throws Exception {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            httpclient.start();
            HttpGet request = new HttpGet("http://www.apache.org/");
            Future<HttpResponse> future = httpclient.execute(request, null);
            HttpResponse response = future.get();
            HttpEntity entity = response.getEntity();
            System.out.println("Response: " + response.getStatusLine());
            System.out.println("Shutting down");
        } finally {
            close(httpclient);
        }
        System.out.println("Done");
    }
    public static final int DEFAULT_BUFFER_SIZE        = 1024 * 4;

    private void doDownloadFile(OutputStream getos, File file) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        try {
            byte[] tempbytes = new byte[DEFAULT_BUFFER_SIZE];
            is = new BufferedInputStream(new FileInputStream(file));
            os = new BufferedOutputStream(getos);

            // I/O 读写
            int byteread = 0;
            while ((byteread = is.read(tempbytes)) != -1)
                os.write(tempbytes, 0, byteread);

            os.flush();
        } finally {
            close(is);
            close(os);
        }
    }

    static void close(CloseableHttpAsyncClient s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(OutputStream s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(InputStream s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(Reader s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }
    static void close(Writer s){
        try{
            if(s != null) s.close();
        }catch (IOException e){}
    }


}