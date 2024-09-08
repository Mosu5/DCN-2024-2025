import com.google.gson.annotations.SerializedName;

/**
 * Created by Arno on 23-3-2017.
 */
public class Book {

    @SerializedName("id")
    private String id;
    @SerializedName("volumeInfo")
    private BookDetail bookDetail;

    public BookDetail getBookDetail() {
        return bookDetail;
    }
}
