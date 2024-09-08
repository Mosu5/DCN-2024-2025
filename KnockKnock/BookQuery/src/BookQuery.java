

import com.google.gson.*;
import java.net.*;
import java.io.*;

public class BookQuery {
    public static void main(String[] args) throws Exception {
        String isbn = args[0];
        URL bookInfo = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn);

//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(bookInfo.openStream()));
//
//        String inputLine;
//        while ((inputLine = in.readLine()) != null)
//            System.out.println(inputLine);
//        in.close();

        InputStream input = bookInfo.openStream();
        Reader reader = new InputStreamReader(input, "UTF-8");
        JsonResult result  = new Gson().fromJson(reader, JsonResult.class);

        //Output
        System.out.println("ISBN: " + isbn );
        System.out.println("Title: " + result.getBookDetail().getTitle() );
        System.out.println("Subtitle: " + result.getBookDetail().getSubTitle() );
        result.getBookDetail().getAuthors().forEach(author -> System.out.println("Author: " + author));
        System.out.println("Description: " + result.getBookDetail().getDescription() );
        System.out.println("Pages: " + result.getBookDetail().getPageCount() );
        System.out.println("Language: " + result.getBookDetail().getLanguage() );
    }


}
