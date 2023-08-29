package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
@SuppressWarnings("unchecked")
public class Commit implements Serializable {
    /** commit message. **/
    private String message;
    /** parent commitID. **/
    private String parent;
    /** commitContents holds fileName and fileSha1 of blobs in a commit. **/
    private TreeMap<String, String> commitContents;

    public Commit(String m, String p, TreeMap<String, String> comContents) {
        this.message = m;
        this.parent = p;
        this.commitContents = comContents;
        SimpleDateFormat formatPls =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        String time = formatPls.format(new Date());
        this.timestamp = time;
        if (this.parent != null) {
            this.timestamp = time;
        } else {
            this.timestamp = formatPls.format(new Date(0));

        }
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return timestamp;

    }
    public String getParent() {
        return this.parent;
    }

    public TreeMap<String, String> getBlobs() {
        return new TreeMap(this.commitContents);
    }
    /** holds timestamp. **/
    private String timestamp;

}
