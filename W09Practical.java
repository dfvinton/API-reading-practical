import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.json.stream.JsonParsingException;

public class W09Practical {
    public static void main(String[] args) {
        boolean wiki = getWikiInput(args);
        boolean inputIsFile = false; // getObj method in Describe.java needs to know what it's getting as input
        try {
            W09Practical prac = new W09Practical();
            Describe desc = new Describe();
            String jsonString = prac.callREST(args, wiki);
            if (!wiki) { // If we're not searching wikipedia (default)
                desc.parseJson(jsonString, inputIsFile);
            } else {
                desc.parseWikiJson(jsonString, inputIsFile);
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL Exception: check query hosts");
        } catch (IOException e) {
            System.out.println("IOException: connection invalid or interrupted");
        }
    }

    // Detect if user inputs decision to query Wikipedia API
    // Also if they input invalid arguments
    public static boolean getWikiInput(String[] args) {
        boolean wiki = false;
        if (args.length != 1 &&  args[1].equals("wiki")) {
            wiki = true;
        } if (args.length != 1 && wiki == false) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }

        return wiki;
    }

    public String getURL(String word, boolean wiki) {
        String first = "http://api.duckduckgo.com/?q=";
        String last = "&format=json&pretty=1";

        if (wiki) { // Using Wikipedia API
            first = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles=";
            last = "&redirects=1"; // Ensures that queries are redirected automatically
        }

        String url = first + word + last;

        return url;
    }

    // Adapted from example class on Studres
    public String callREST(String[] args, boolean wiki) throws IOException, MalformedURLException {
        URL url = new URL(getURL(args[0], wiki));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //decompose conn
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("User-Agent", "curl/7.37.0");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder buffer = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            buffer.append(output);
        }
        conn.disconnect();

        return buffer.toString();
    }


}