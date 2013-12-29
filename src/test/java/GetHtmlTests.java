import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.jsoup.select.Elements;
import java.io.IOException;

/**
 * Created by Stony on 13-12-29.
 */
public class GetHtmlTests {

    static final String MATH_URL = "spicy.althemist.com";
    @Test
    public void get() throws IOException {
        String uri = "http://spicy.althemist.com/index.php";
        int timeoutMillis = 20000;
        Document doc = Jsoup.connect(uri).timeout(timeoutMillis).get();
//        System.out.println(doc.html());

        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

        print("\nMedia: (%d)", media.size());
        for (Element src : media) {
            if (src.tagName().equals("img"))
                print(" * %s: <%s> %sx%s (%s)",
                        src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                        trim(src.attr("alt"), 20));
            else
                print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
        }

        print("\nImports: (%d)", imports.size());
        for (Element link : imports) {
            print(" * %s <%s> (%s)", link.tagName(),link.attr("abs:href"), link.attr("rel"));
        }

        print("\nLinks: (%d)", links.size());
        for (Element link : links) {
            print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
    static boolean isMath(String url){
        return url.indexOf(MATH_URL) != -1;
    }
    static boolean isUrl(String url){
        return url.startsWith("http:") || url.startsWith("https:");
    }
}
