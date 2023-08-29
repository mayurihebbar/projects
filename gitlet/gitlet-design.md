# Gitlet Design Document
author: Mayuri Hebbar

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main
*  main method that holds function calls
### Object:
* Instance Variables
  * Message - a string that holds the commit message
  * Timestamp - string (for now) that holds the time at which commit was made
  * Parent - a commit object that is the parent of the commit object
### StagedArea
* addedCommits: holds the most recent git add object

## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.

### Main
* where directory is created
* commits are added to branch
* commands are run  here
* holds head pointer to the most recent commit or the working commit from the branch
  * Previous Heads: arraylist of previous commit heads
  * current head: object containing the current head
### Staged Area
* holds an object after git add command has been called, prepares to add to branch
* not too sure what else needs to be done, basically just takes note of any changes 
### Object
* Init:
  * creates a new git repository 
  * Variable - WorkingGit: new git object with initial values null
  * create a new blob object (blobInitial) at the time of initialization 
* Add:
  * updates the stagedArea with the new changes to the directory, preparing for a commit
  * should check SHA-1 id to check that a unique SHA-1 id is passed in
    * if SHA-1 id passed in has already been added, then it should not be added to staging area
  * Create a new blob object every time a new modification is about to be staged
    * every new commit should have a blob object 
  
* Commit 
  * adds the modifications to the working branch. pointers to master and head 
  * need to be updated accordingly
  * needs to keep track of date & time of commit
  * clears staging area 
  * create branch object 
    * points to  most recently added commit on the branch or the current commit as specified by checkout master
  * multiword messages need to be put in quotes
  * create a branch object (probably a hashmap) that holds the commit details (timestamp & message) 
    * branch needs to account for when branch is empty --> should give error that init needs to be run
  * SHA-1 id includes blob references of the files, parent reference, log message and commit time
  * every time commit is executed, a new commit object is created that is added to the branch
  * HANDLE MERGE CONFLICTS: check to see that the file has not been modified by other branches
  * update the contents of the (blobInitial) and add it to the branch on the commit object
* Log 
  * creates and stores a commit code to track the commit history 
  * every time there is a commit, there needs to be a new commit code created that can be traced back to the commit object on the branch history 
  * could possibly use a hashmap for this, to map between the code and the commit object
  * displays commit code, commit time, and commit message
* Checkout 
  * could again use hashmap to go back and map from the code to the commit object that you are trying to go back to in the branch
  * essentially moves the referenced node (commit) to the current working directory
* OtherCommands 
  * other classes need to be added for the other commands


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

Mayuri's Methodology:

* we want to save the following info:
  * Commits being made --> the specific files and contents that are being made in every commit
  * Number of commits, the commit code (to update the git log)
  * making sure the commit sequence is accurate 
    * (using maybe time stamp or a stack, to access the commits chronologically)

When to Save Commits (Persistence)
1. after git add is called, we need to store which files "git add" was being called on
2. until the next git commit -m "comment" is called we hold on to the previously added, so that when git commit is called we backtrack to the most recent files we have on hold
3. Data needed across multiple calls to gitlet: the files modified, the files added, and files most recently commited
   1. within the working directory we need to keep track of the actual contents 
      1. keep track of blobs, trees, commits, and tags

Can use serializable to store and convert data 
## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

![](/Users/mayurihebbar/Desktop/IMG_2636.jpeg)

