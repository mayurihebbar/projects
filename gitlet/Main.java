package gitlet;

import java.io.IOException;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Mayuri Hebbar
 * @collaborators: Ashana Makhija, Shivani Sahni, Chuyi Shang, Aditya Biswal
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args) throws IOException {
        Commands newObject = new Commands();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case ("init"):
            newObject.init(); break;
        case ("add"):
            newObject.add(args[1]); break;
        case ("commit"):
            newObject.commit(args[1]); break;
        case ("checkout"):
            if (args.length == 3) {
                newObject.checkoutFile(args);
            } else if (args.length == 4) {
                newObject.checkoutFileCommit(args);
            } else {
                newObject.checkoutBranch(args[1]);
            }
            break;

        case ("log"):
            newObject.log(); break;

        case("rm"):
            newObject.rm(args[1]); break;

        case ("global-log"):
            newObject.globalLog(); break;
        case ("find"):
            newObject.find(args[1]); break;
        case ("status"):
            newObject.status(); break;
        case ("branch"):
            newObject.branch(args[1]); break;
        case ("rm-branch"):
            newObject.rmBranch(args[1]); break;
        case ("reset"):
            newObject.reset(args[1]); break;
        case ("merge"):
            newObject.merge(args[1]); break;
        case ("add-remote"):
            newObject.addRemote(args); break;
        case ("rm-remote"):
            newObject.rmRemote(args); break;
        case ("fetch"):
            newObject.fetch(args); break;
        case ("push"):
            newObject.push(args); break;

        default:
            System.out.println("No command with that name exists.");
            System.exit(0);

        }

    }
}
