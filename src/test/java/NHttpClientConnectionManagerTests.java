import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.Test;

/**
 * Created by Stony on 13-12-29.
 */
public class NHttpClientConnectionManagerTests {


    @Test
    public void asyncMgr() throws IOReactorException {
        PoolingNHttpClientConnectionManager asyncMgr = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor());

        asyncMgr.setMaxTotal(30);
        asyncMgr.setDefaultMaxPerRoute(10);


        CloseableHttpAsyncClient asy = HttpAsyncClients.createMinimal(asyncMgr);
    }
}
