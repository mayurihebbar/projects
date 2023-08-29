package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
public class Remote implements Serializable {
    /** holds the remote branch name and path. **/
    private TreeMap<String, String> getRemBranchMap = new TreeMap<>();
    public TreeMap<String, String> getRemBranchMap() {
        return getRemBranchMap;
    }





}
