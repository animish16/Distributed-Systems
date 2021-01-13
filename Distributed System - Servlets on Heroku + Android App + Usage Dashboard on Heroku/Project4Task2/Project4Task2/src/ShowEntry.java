import java.util.Objects;

public class ShowEntry implements Comparable<ShowEntry> {
    String showName;
    int count;

    ShowEntry(String showName) {
        // Initialize the show entry
        this.showName = showName;
        this.count = 1;
    }

    /**
     * Increment the show count
     */
    void incrementCount() {
        this.count++;
    }

    /**
     * Overwriting hashCode for equality while processing in  LogItem class
     * @return
     */
    public int hashCode() {
        return Objects.hash(showName);
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
        ShowEntry s = (ShowEntry) o;
        return showName.equalsIgnoreCase(s.showName);
    }

    /**
     * Overwriting compareTo for sorting by count
     * @param s
     * @return
     */
    public int compareTo(ShowEntry s) {
        return this.count < s.count ? 1 : -1;
    }
}
