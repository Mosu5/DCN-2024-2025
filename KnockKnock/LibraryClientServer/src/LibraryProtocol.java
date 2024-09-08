import com.google.gson.*;
import java.net.*;
import java.io.*;

public class LibraryProtocol {

    private static final int WAITING = 0;
    private static final int QUERY = 1;

    private int state = WAITING;

    public String processInput(String theInput) {
        String theOutput = null;

        if (state == WAITING) {
            theOutput = "Give the isbn of the book you want to search for";
            state = QUERY;
        } else if (state == QUERY) {
            if (theInput == null) {
                theOutput = "You're supposed to say the isbn of the book you want to search for! Try again.";
                state = WAITING;
                return theOutput;
            }
            try {
                theOutput = handleQuery(theInput);
            } catch (Exception e) {
                theOutput = "An error occurred while searching for the book. Please try again.";
                e.printStackTrace();
            }
            state = WAITING;
        }
        return theOutput;
    }

    private String handleQuery(String theInput) throws Exception {
        // check if the input is a valid isbn
        if (!theInput.matches("^(97(8|9))?[0-9]{10}$")) {
            System.out.println("Invalid isbn");
            return "Invalid isbn";
        }
        String isbn = theInput;
        URL bookInfo = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn);
        InputStream input = bookInfo.openStream();
        Reader reader = new InputStreamReader(input, "UTF-8");
        JsonResult result = new Gson().fromJson(reader, JsonResult.class);
        // convert result to string
        String output = "ISBN: " + isbn + "\n"
                + "Title: " + result.getBookDetail().getTitle() + "\n"
                + "Subtitle: " + result.getBookDetail().getSubTitle() + "\n";
        return output;
    }
}
