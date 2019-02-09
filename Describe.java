import java.util.*;
import java.io.*;
import javax.json.*;
import javax.json.stream.JsonParsingException;

public class Describe {
    public static void main(String[] args) {
        boolean inputIsFile = true; // getObj method below needs to know if input it's getting is a string or file path

        if (args.length != 1) {
            System.out.println("Invalid number of arguments");
            System.exit(1);
        }

        try {
            String input = args[0];
            Describe desc = new Describe();
            desc.parseJson(input, inputIsFile);
        } catch (JsonParsingException e) { // Exceptions are thrown to be caught here; I've dealt with non-fatal problems below
            System.out.println("Not a valid JSON string!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException: file in unexpected format or not found");
            System.exit(1);
        }
    }

        // If heading field exists, execute child methods
        // If it's blank, search didn't return anything so we inform user
        // Informs user and ends if method encounters non-existant header
    public void parseJson(String input, boolean inputIsFile) throws JsonParsingException, IOException {
        JsonObject mainObj = getObj(input, inputIsFile);
        if ((mainObj.get("Heading").toString().equals(""))) {
            System.out.println("No results for that search");
        } else if (mainObj.containsKey("Heading")) {
            System.out.println(mainObj.get("Heading").toString() + " can refer to:");
            printRelatedTopics(mainObj);
        } else {
            System.out.println("Not a valid JSON string!");
            System.exit(1);
        }
    }

    // This class is used by itself to read from a file and with W09Practical to read a string
    // So this creates two different kinds of reader for our two different kinds of input
    public JsonObject getObj(String input, boolean inputIsFile) throws JsonParsingException, IOException {
        JsonReader reader = Json.createReader(new StringReader(input));

        if (inputIsFile) {
            reader = Json.createReader(new FileReader(input));
        }

        JsonObject obj = reader.readObject();
        reader.close();

        return obj;
    }

    // Checks if any topics have sub-topics, prints them if they have text
    public void printRelatedTopics(JsonObject mainObj) throws JsonParsingException, IOException {
        JsonArray relatedTopics = mainObj.getJsonArray("RelatedTopics");

        for (int i = 0; i < relatedTopics.size(); i++) {
            JsonObject relatedObj = relatedTopics.getJsonObject(i);
            if (relatedObj.containsKey("Topics") == true) { // If there are any sub-topics, go to method below
                printSubTopics(relatedObj);
            } else if (relatedObj.containsKey("Text") && !(relatedObj.get("Text").toString().equals(""))) { // Otherwise, print object text
                System.out.println("  - " + relatedObj.get("Text").toString());
            }
        }

    }

    // Prints sub-topics and their text, if it exists
    public void printSubTopics(JsonObject relatedObj) throws JsonParsingException, IOException {
        JsonArray subTopics = relatedObj.getJsonArray("Topics");
        if (subTopics.size() > 0) { // If subTopics is not empty
            System.out.println("  * Category: " + relatedObj.get("Name"));
            for (int j = 0; j < subTopics.size(); j++) {
                JsonObject subTopicObj = subTopics.getJsonObject(j);
                if (subTopicObj.containsKey("Text") && !(subTopicObj.get("Text").toString().equals(""))) {
                    System.out.println("    - " + subTopicObj.get("Text"));
                }
            }
        }

    }

    // Gets page title and extract from JSON we've received
    // We don't know the name of the nested JSON array that will contain what we need, so we need to find it out
    // Code adapted from http://stackoverflow.com/questions/1018750/how-to-convert-object-array-to-string-array-in-java
    // Wikipedia JSON formatting help from //http://stackoverflow.com/questions/8555320/is-there-a-clean-wikipedia-api-just-for-retrieve-content-summary/18504997#18504997
    public void parseWikiJson(String jsonString, boolean inputIsFile) throws IOException, JsonParsingException {
        JsonObject mainObj = getObj(jsonString, inputIsFile);
        JsonObject queryObj = mainObj.getJsonObject("query");
        JsonObject pagesObj = queryObj.getJsonObject("pages"); // Going through layers of nested objects

        String pageKey = getPageKey(pagesObj);
        JsonObject pageContents = pagesObj.getJsonObject(pageKey);

        String pageTitle = "";
        String pageExtract = "";

        if (pageContents.containsKey("title") && !(pageContents.get("title").toString().equals(""))) {
            pageTitle = pageContents.get("title").toString();
            System.out.println("\nWikipedia for " + pageTitle + ":" + "\n");
        } else {
            System.out.println("Page title unavailable");
        } if (pageContents.containsKey("extract") && !(pageContents.get("extract").toString().equals(""))) {
            pageExtract = pageContents.get("extract").toString();
            printWikipediaExtract(pageExtract);
        } else {
            System.out.println("Page content doesn't exist or is unavailable");
        }
    }

    public String getPageKey(JsonObject pagesObj) {
        // Gets the keySet from pagesObj, casts it as array of objects, casts that as array of strings
        String[] keys = Arrays.asList(pagesObj.keySet().toArray()).toArray(new String[pagesObj.keySet().toArray().length]);
        String pageKey = keys[0]; // There's only one member in the array

        return pageKey;
    }

    // Formats and prints Wikipedia query
    public void printWikipediaExtract(String pageExtract) {

        String[] lines = pageExtract.split("\\r?\\n"); // Splits where there would be newlines on Wikipedia so it looks good on terminal
                                                       // Regex from http://stackoverflow.com/questions/454908/split-java-string-by-new-line
        for (int i = 0; i < lines.length; i++) {
            System.out.println(lines[i] + "\n");
        }
    }
}


