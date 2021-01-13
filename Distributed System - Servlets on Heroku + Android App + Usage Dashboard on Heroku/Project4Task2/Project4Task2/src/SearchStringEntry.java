import java.util.Objects;

public class SearchStringEntry implements Comparable<SearchStringEntry> {
    String searchString;
    String latestShowResult;
    int count;

    public SearchStringEntry(String searchString) {
        // Initialize search string entry
        this.searchString = searchString;
        this.latestShowResult = null;
        this.count = 1;
    }

    /**
     * Increment the search count
     */
    void incrementCount() {
        this.count++;
    }

    /**
     * setter for the most recent (latest) matching TV show for the search query
     * @param latestShowResult
     */
    void setLatestShowResult(String latestShowResult) {
        if (latestShowResult == null || latestShowResult.equals("")) {
            // If query doesn't have a matching show, store no result
            this.latestShowResult = "<em>&laquo;no result&raquo;</em>";
        } else {
            // else, store the show name
            this.latestShowResult = latestShowResult;
        }
    }

    /**
     * Overwriting hashCode for equality while processing in  LogItem class
     * @return
     */
    public int hashCode() {
        return Objects.hash(searchString);
    }

    /**
     * Overwriting equals for equality while processing in  LogItem class
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        SearchStringEntry s = (SearchStringEntry) o;
        return searchString.equalsIgnoreCase(s.searchString);
    }

    /**
     * Overwriting compareTo for sorting by count
     * @param s
     * @return
     */
    public int compareTo(SearchStringEntry s) {
        return this.count < s.count ? 1 : -1;
    }
}
