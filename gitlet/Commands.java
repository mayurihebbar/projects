package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.TreeMap;
import java.util.ArrayList;

import static gitlet.Utils.*;
@SuppressWarnings("unchecked")
public class Commands implements Serializable {
    /** comment. */
    private static File cwd = new File(System.getProperty("user.dir"));
    /** comment. */
    static final File GITLET_FOLDER = new File(cwd, ".gitlet");
    /** comment. */
    private String message;
    /** comment. */
    private Commit parent;
    /** head stores commitID of head commit. */
    private String head;
    /** comment. */
    private File blobs = Utils.join(GITLET_FOLDER, "blobs");
    /** comment. */
    private File stagingRemove = Utils.join(GITLET_FOLDER, "stagingRemove");
    /** comment. */
    private File stagingRemoveFile =
            Utils.join(GITLET_FOLDER, "stagingRemoveFile");
    /** comment. */
    private File stagingAdd = Utils.join(GITLET_FOLDER, "stagingAdd");
    /** comment. */
    private File stagingArea = Utils.join(GITLET_FOLDER, "stagingArea");
    /** comment. */
    private File stagingAreaFile = Utils.join(stagingArea,  "stagingAreaFile");
    /** comment. */
    private File commits = Utils.join(GITLET_FOLDER, "commits");
    /** comment. */
    private File commitBranchFile = Utils.join(commits, "commitBranchFile");
    /** comment. */
    private File commitContentsFile = Utils.join(commits, "commitContentsFile");
    /** comment. */
    private File headFolder = Utils.join(GITLET_FOLDER, "head");
    /** comment. */
    private File headFile = Utils.join(headFolder, "headFile");
    /** comment. */
    private File branches = Utils.join(GITLET_FOLDER, "branches");
    /** holds Branches TreeMap. */
    private File branchFile = Utils.join(branches, "branchFile");
    /** holds string w current branch name. */
    private File currentBranch = Utils.join(branches, "currentBranch");
    /** comment. */
    private File remote = Utils.join(GITLET_FOLDER, "remote");
    /** comment. */
    private Remote remoteTracker = new Remote();


    public void init() throws IOException {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
        } else {
            GITLET_FOLDER.mkdirs();
            stagingArea.mkdirs();
            stagingAdd.mkdirs();
            stagingRemove.mkdirs();
            blobs.mkdirs();
            commits.mkdirs();
            headFolder.mkdirs();
            branches.mkdirs();
            Commit initial =
                    new Commit("initial commit", null, commitContents);
            String initialCommitID =
                    Utils.sha1(Utils.serialize(initial.getBlobs()));

            commitBranch.put(initialCommitID, initial);
            headObject.put(initialCommitID, initial);
            commitBranchFile.createNewFile();
            Utils.writeObject(commitBranchFile, commitBranch);

            parent = commitBranch.lastEntry().getValue();
            headFile.createNewFile();
            Utils.writeObject(headFile, headObject);
            TreeMap<String, String> stagingTree = new TreeMap<String, String>();
            stagingAreaFile.createNewFile();
            Utils.writeObject(stagingAreaFile, stagingTree);
            Utils.writeObject(commitContentsFile, commitContents);
            TreeMap<String, String> removalTree = new TreeMap<String, String>();
            stagingRemoveFile.createNewFile();
            Utils.writeObject(stagingRemoveFile, removalTree);
            branchTracker = "master";
            branchMap.put("master", initial);
            Utils.writeObject(currentBranch, branchTracker);
            Utils.writeObject(branchFile, branchMap);

            Utils.writeObject(remote, remoteTracker);

        }

    }
    public void add(String fileName) throws IOException {
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        stageRemove = Utils.readObject(stagingRemoveFile, TreeMap.class);
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        parent = headObject.lastEntry().getValue();
        File fileChecker = gitlet.Utils.join(cwd, fileName);
        if (fileChecker.exists()) {
            byte[] fcContent = Utils.readContents(fileChecker);
            String fileID = Utils.sha1(Utils.serialize(fcContent));
            if (parent.getBlobs().containsKey(fileName)) {
                if (!(parent.getBlobs().get(fileName).equals(fileID))) {
                    stagingAreaMap.put(fileName, fileID);
                    File newBlob = Utils.join(blobs, fileID);
                    newBlob.createNewFile();
                    Utils.writeContents(newBlob, readContents(fileChecker));
                } else {
                    if (stagingAreaMap.containsKey(fileName)) {
                        stagingAreaMap.remove(fileName);
                    }
                    if (stageRemove.containsKey(fileName)) {
                        stageRemove.remove(fileName);
                    }
                }
            } else {
                if (stageRemove.containsKey(fileName)) {
                    stageRemove.remove(fileName);
                } else {
                    stagingAreaMap.put(fileName, fileID);
                    File newBlob = Utils.join(blobs, fileID);
                    newBlob.createNewFile();
                    Utils.writeContents(newBlob, readContents(fileChecker));
                    Utils.writeObject(stagingAreaFile, stagingAreaMap);
                }
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Utils.writeObject(stagingRemoveFile, stageRemove);
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
        Utils.writeObject(commitContentsFile, commitContents);
    }



    public void commit(String mes) {
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        stageRemove = Utils.readObject(stagingRemoveFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        if (stagingAreaMap.size() == 0 && stageRemove.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (mes.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else if (branchTracker.equals("master")) {
            this.message = mes;
            String currentCommitID = (headObject.lastEntry().getKey());
            Commit cloneCommit = commitBranch.get(currentCommitID);
            TreeMap<String, String> files = cloneCommit.getBlobs();
            for (String fileName : stagingAreaMap.keySet()) {
                files.put(fileName, stagingAreaMap.get(fileName));
            }
            for (String rfn : stageRemove.keySet()) {
                if (cloneCommit.getBlobs().containsKey(rfn)) {
                    files.remove(rfn, stageRemove.get(rfn));
                }
            }
            Commit newCommit = new Commit(mes, currentCommitID, files);
            String newCommitID = Utils.sha1(Utils.serialize(newCommit));
            commitBranch.put(newCommitID, newCommit);
            headObject.clear(); headObject.put(newCommitID, newCommit);
            branchMap.put(branchTracker, headObject.firstEntry().getValue());
            stagingAreaMap.clear(); stageRemove.clear();
        } else {
            this.message = mes;
            Commit cloneCommit = headObject.firstEntry().getValue();
            TreeMap<String, String> files = cloneCommit.getBlobs();
            for (String fileName : stagingAreaMap.keySet()) {
                files.put(fileName, stagingAreaMap.get(fileName));
            }
            for (String rmFileName : stageRemove.keySet()) {
                if (cloneCommit.getBlobs().containsKey(rmFileName)) {
                    files.remove(rmFileName, stageRemove.get(rmFileName));
                }
            }
            Commit nc = new Commit(mes, headObject.lastEntry().getKey(), files);
            String newCommitID = Utils.sha1(Utils.serialize(nc));
            branchMap.replace(branchTracker, nc);
            commitBranch.put(newCommitID, nc);
            headObject.clear(); headObject.put(newCommitID, nc);
            stagingAreaMap.clear(); stageRemove.clear();
        }
        Utils.writeObject(stagingRemoveFile, stageRemove);
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
        Utils.writeObject(commitContentsFile, commitContents);
        Utils.writeObject(headFile, headObject);
        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(currentBranch, branchTracker);
    }

    public void checkoutFile(String... args) throws IOException {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        Commit headCommit = commitBranch.get(headObject.lastEntry().getKey());
        Utils.readContents(headFile);

        if (headCommit.getBlobs().containsKey(args[2])) {
            commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
            stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
            commitContents =
                    Utils.readObject(commitContentsFile, TreeMap.class);
            File checkoutFile =
                    Utils.join(blobs, headCommit.getBlobs().get(args[2]));
            File updatedCheckout = Utils.join(cwd, args[2]);
            if (updatedCheckout.exists()) {
                byte[] checkoutFileContents = Utils.readContents(checkoutFile);
                Utils.writeContents(updatedCheckout, checkoutFileContents);
            } else {
                updatedCheckout.createNewFile();
                byte[] checkoutFileContents = Utils.readContents(checkoutFile);
                Utils.writeContents(updatedCheckout, checkoutFileContents);

            }


            Utils.writeObject(commitBranchFile, commitBranch);
            Utils.writeObject(stagingAreaFile, stagingAreaMap);
            Utils.writeObject(commitContentsFile, commitContents);

        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

    }

    public void checkoutFileCommit(String... args) throws IOException {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        if (!(args[2].equals("--"))) {
            System.out.println("Incorrect operands.");
            Utils.writeObject(commitBranchFile, commitBranch);
            Utils.writeObject(stagingAreaFile, stagingAreaMap);
            System.exit(0);
        }
        String forUID = args[1];
        String fn = args[3];
        if (commitBranch.containsKey(args[1]) || (uidhelper(forUID))) {
            Commit checkoutCommit = getCheckoutCommit(forUID);
            if (checkoutCommit.getBlobs().containsKey(fn)) {
                File cf = Utils.join(blobs, checkoutCommit.getBlobs().get(fn));
                File updatedCheckout = Utils.join(cwd, fn);
                if (updatedCheckout.exists()) {
                    byte[] cfContents = Utils.readContents(cf);
                    Utils.writeContents(updatedCheckout, cfContents);
                } else {
                    updatedCheckout.createNewFile();
                    String cfContents = Utils.readContentsAsString(cf);
                    Utils.writeContents(updatedCheckout, cfContents);
                }
                Utils.writeObject(commitBranchFile, commitBranch);
                Utils.writeObject(stagingAreaFile, stagingAreaMap);
                return;
            } else {
                System.out.println("File does not exist in that commit.");
            }
        } else {
            System.out.println("No commit with that id exists.");
        }
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
    }
    public boolean uidhelper(String forUID) {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        for (String cID : commitBranch.keySet()) {
            if (cID.contains(forUID)) {
                return true;
            }
        }
        return false;

    }

    public Commit getCheckoutCommit(String forUID) {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        for (String cID : commitBranch.keySet()) {
            if (cID.contains(forUID)) {
                Commit checkoutCommit = commitBranch.get(cID);
                return checkoutCommit;
            }
        }
        return null;
    }
    public void checkoutBranch(String... args) throws IOException {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        for (String fileName : Utils.plainFilenamesIn(cwd)) {
            if (!(branchMap.get(branchTracker).getBlobs().containsKey(fileName))
                    && (!(stagingAreaMap.containsKey(fileName)))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        if (branchTracker.equals(args[0])) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        } else if ((!(branchMap.containsKey(args[0])))) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else {
            TreeMap<String, String> currentBranchFiles = new TreeMap<>();
            currentBranchFiles = headObject.firstEntry().getValue().getBlobs();
            branchTracker = args[0];
            Commit headCommit = branchMap.get(branchTracker);
            String headCommitID = Utils.sha1(Utils.serialize(headCommit));
            for (String currentBranchFile : currentBranchFiles.keySet()) {
                if (!(headCommit.getBlobs().containsKey(currentBranchFile))) {
                    Utils.restrictedDelete(currentBranchFile);
                }
            }
            if (headCommit.getBlobs().size() == 0) {
                headObject.clear();
                headObject.put(headCommitID, headCommit);
            } else {
                for (String f : headCommit.getBlobs().keySet()) {
                    File cf = Utils.join(blobs, headCommit.getBlobs().get(f));
                    File updatedCheckout = Utils.join(cwd, f);
                    if (updatedCheckout.exists()) {
                        byte[] cfContents = Utils.readContents(cf);
                        Utils.writeContents(updatedCheckout, cfContents);
                    } else {
                        updatedCheckout.createNewFile();
                        String cfContents = Utils.readContentsAsString(cf);
                        Utils.writeContents(updatedCheckout, cfContents);
                    }
                }
                headObject.clear();
                headObject.put(headCommitID, headCommit);
            }
        }
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
        Utils.writeObject(commitContentsFile, commitContents);
        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(currentBranch, branchTracker);
        Utils.writeObject(headFile, headObject);
    }


    public void log() {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        head = headObject.firstEntry().getKey();
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        Commit nhCommit = branchMap.get(branchTracker);
        String newHead = Utils.sha1(Utils.serialize(nhCommit));
        while (newHead != null) {
            Commit currentCommit = commitBranch.get(newHead);
            System.out.println("===");
            System.out.println("commit " + newHead);
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();
            newHead = currentCommit.getParent();
        }

        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);

    }
    public void rm(String... args) {
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        stageRemove = Utils.readObject(stagingRemoveFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        head = headObject.firstEntry().getKey();
        String removeFile = args[0];
        String removeFileID = commitBranch.get(head).getBlobs().get(removeFile);

        if (stagingAreaMap.containsKey(removeFile)) {
            stagingAreaMap.remove(removeFile);
        } else if (commitBranch.get(head).getBlobs().containsKey(removeFile)) {
            stageRemove.put(removeFile, removeFileID);
            Utils.restrictedDelete(removeFile);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingRemoveFile, stageRemove);
        Utils.writeObject(headFile, headObject);

    }
    public void globalLog() {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        head = headObject.firstEntry().getKey();

        while (head != null) {
            Commit currentCommit = commitBranch.get(head);
            System.out.println("===");
            System.out.println("commit " + head);
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();
            head = currentCommit.getParent();
        }

        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);


    }
    public void find(String... args) {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        ArrayList<String> checker = new ArrayList<String>();
        for (String cID : commitBranch.keySet()) {
            if (commitBranch.get(cID).getMessage().equals(args[0])) {
                checker.add(cID);
                System.out.println(cID);
            }
        }
        if (checker.size() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);

    }
    public void status() {
        if (!(GITLET_FOLDER.exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);

        }
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        stageRemove = Utils.readObject(stagingRemoveFile, TreeMap.class);
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);

        System.out.println("=== Branches ===");
        for (String branchName : branchMap.keySet()) {
            if (branchName.equals(branchTracker)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileName : stagingAreaMap.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String removeFileName : stageRemove.keySet()) {
            System.out.println(removeFileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");


    }
    public void branch(String... args) {
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        headObject = Utils.readObject(headFile, TreeMap.class);


        if ((branchMap.containsKey(args[0]))) {
            System.out.println("A branch with that name already exists.");

        } else {
            head = headObject.firstEntry().getKey();
            branchMap.put(args[0], headObject.get(head));

        }


        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(currentBranch, branchTracker);
        Utils.writeObject(headFile, headObject);




    }
    public void rmBranch(String...args) {
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);

        if (branchTracker.equals(args[0])) {
            System.out.println("Cannot remove the current branch.");
        } else if (!(branchMap.containsKey(args[0]))) {
            System.out.println("A branch with that name does not exist.");
        } else {
            branchMap.remove(args[0]);
        }

        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(currentBranch, branchTracker);
        Utils.writeObject(headFile, headObject);
        Utils.writeObject(commitBranchFile, commitBranch);



    }
    public void reset(String... args) throws IOException {
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        commitContents = Utils.readObject(commitContentsFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        Commit checkoutCommit = commitBranch.get(args[0]);
        if (!(commitBranch.containsKey(args[0]))) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        headObject = Utils.readObject(headFile, TreeMap.class);
        for (String fn : Utils.plainFilenamesIn(cwd)) {
            if (!(headObject.firstEntry().getValue().getBlobs().containsKey(fn))
                    && (!(stagingAreaMap.containsKey(fn)))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
            if (stagingAreaMap.containsKey(fn)) {
                Utils.restrictedDelete(fn);
            }
            if (headObject.firstEntry().getValue().getBlobs().containsKey(fn)
                    && (!(checkoutCommit.getBlobs().containsKey(fn)))) {
                Utils.restrictedDelete(fn);
            }
        }
        for (String fn : checkoutCommit.getBlobs().keySet()) {
            File cf = Utils.join(blobs, checkoutCommit.getBlobs().get(fn));
            File updatedCheckout = Utils.join(cwd, fn);
            if (updatedCheckout.exists()) {
                byte[] checkoutFileContents = Utils.readContents(cf);
                Utils.writeContents(updatedCheckout, checkoutFileContents);
            } else {
                updatedCheckout.createNewFile();
                String cfContents = Utils.readContentsAsString(cf);
                Utils.writeContents(updatedCheckout, cfContents);

            }
            Utils.writeObject(commitBranchFile, commitBranch);
            Utils.writeObject(stagingAreaFile, stagingAreaMap);
        }
        stagingAreaMap.clear();
        branchMap.replace(branchTracker, checkoutCommit);
        Utils.writeObject(headFile, headObject);
        Utils.writeObject(currentBranch, branchTracker);
        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(commitBranchFile, commitBranch);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);





    }
    public void merge(String... args) throws IOException {
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        stageRemove = Utils.readObject(stagingRemoveFile, TreeMap.class);
        String givenBranch = args[0];
        String curBranch = branchTracker;
        for (String fn : Utils.plainFilenamesIn(cwd)) {
            if (!(headObject.firstEntry().getValue().getBlobs().containsKey(fn))
                    && (!(stagingAreaMap.containsKey(fn)))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        if (!(branchMap.containsKey(givenBranch))) {
            System.out.println(" A branch with that name does not exist.");
            System.exit(0);
        } else if ((stagingAreaMap.size() != 0) | (stageRemove.size() != 0)) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (branchTracker.equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else {
            Commit lca = getLCA(givenBranch, curBranch);
            ArrayList<String> currentBranchAncestors = new ArrayList<>();
            Commit currentBranchHead = branchMap.get(branchTracker);
            Commit givenBranchHead = branchMap.get(givenBranch);
            if (givenBranchHead.equals(lca)) {
                System.out.println("Given branch is an "
                        + "ancestor of the current branch.");
                System.exit(0);
            }
            if (currentBranchHead.equals(lca)) {
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            }

        }
        Utils.writeObject(stagingRemoveFile, stageRemove);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(currentBranch, branchTracker);
        Utils.writeObject(headFile, headObject);
        Utils.writeObject(commitBranchFile, commitBranch);
    }
    private Commit getLCA(String mergeBranch, String curBranch) {
        branchMap = Utils.readObject(branchFile, TreeMap.class);
        branchTracker = Utils.readObject(currentBranch, String.class);
        headObject = Utils.readObject(headFile, TreeMap.class);
        commitBranch = Utils.readObject(commitBranchFile, TreeMap.class);
        stagingAreaMap = Utils.readObject(stagingAreaFile, TreeMap.class);
        stageRemove = Utils.readObject(stagingRemoveFile, TreeMap.class);
        ArrayList<Commit> givenBranchAnc = new ArrayList<>();
        Commit givenBranchHead = branchMap.get(mergeBranch);
        while (givenBranchHead != null) {
            if (givenBranchHead.getParent() == null) {
                givenBranchAnc.add(givenBranchHead);
                break;
            } else {
                givenBranchAnc.add(givenBranchHead);
                givenBranchHead =
                        commitBranch.get(givenBranchHead.getParent());
            }

        }
        ArrayList<Commit> currentBranchAnc = new ArrayList<>();
        Commit cbHead = branchMap.get(branchTracker);
        while (cbHead != null) {
            if (cbHead.getParent() == null) {
                currentBranchAnc.add(cbHead);
                break;
            } else {
                currentBranchAnc.add(cbHead);
                cbHead = commitBranch.get(cbHead.getParent());

            }
        }
        Commit lcaCommit;
        for (int i = 0; i < givenBranchAnc.size(); i++) {
            for (int j = 0; j < currentBranchAnc.size(); j++) {
                if (givenBranchAnc.get(i).equals(currentBranchAnc.get(j))) {
                    lcaCommit = givenBranchAnc.get(i);
                    return lcaCommit;
                }
            }
        }
        Utils.writeObject(stagingRemoveFile, stageRemove);
        Utils.writeObject(stagingAreaFile, stagingAreaMap);
        Utils.writeObject(branchFile, branchMap);
        Utils.writeObject(currentBranch, branchTracker);
        Utils.writeObject(headFile, headObject);
        Utils.writeObject(commitBranchFile, commitBranch);
        return null;
    }
    public void addRemote(String... args) {
        remoteTracker = Utils.readObject(remote, Remote.class);
        String name = args[1];
        if (remoteTracker.getRemBranchMap().containsKey(name)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        remoteTracker.getRemBranchMap().put(name, args[2]);
        Utils.writeObject(remote, remoteTracker);
    }

    public void rmRemote(String...args) {
        remoteTracker = Utils.readObject(remote, Remote.class);
        String name = args[1];
        if (remoteTracker.getRemBranchMap().containsKey(name)) {
            remoteTracker.getRemBranchMap().remove(name);
        } else {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        Utils.writeObject(remote, remoteTracker);
    }

    public void fetch(String... args) {
        remoteTracker = Utils.readObject(remote, Remote.class);
        String name = args[1];
        if ((new File(cwd,
                remoteTracker.getRemBranchMap().get(name)).exists())) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        } else {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Utils.writeObject(remote, remoteTracker);

    }

    public void push(String... args) {
        remoteTracker = Utils.readObject(remote, Remote.class);
        String name = args[1];
        if ((new File(cwd,
                remoteTracker.getRemBranchMap().get(name)).exists())) {
            System.out.println("Please pull "
                    + "down remote changes before pushing.");
            System.exit(0);
        } else {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        Utils.writeObject(remote, remoteTracker);
    }


    /** key is commit sha1 and value is commit object. **/
    private TreeMap<String, Commit> commitBranch = new TreeMap<>();
    /** key is file Name and value is fileSha1. **/
    private TreeMap<String, String> stagingAreaMap = new TreeMap<>();
    /** key is file Name and value is fileSha1. **/
    private TreeMap<String, String> stageRemove = new TreeMap<>();
    /** key is fileNames in the commit and  value is file sha1. **/
    private TreeMap<String, String> commitContents = new TreeMap<>();
    /** key is head commit ID and value is head commit object. **/
    private TreeMap<String, Commit> headObject = new TreeMap<>();
    /** key is branch name and value is head commit. **/
    private TreeMap<String, Commit> branchMap = new TreeMap<>();
    /** holds string with current branch name. **/
    private String branchTracker;



}
