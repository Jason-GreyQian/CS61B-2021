package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author GreyQian
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // what if args is empty?
        if (args.length < 1) {
            MyUtils.exit("Please enter a command.");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                MyUtils.validateOperands(args, 1);
                Repository.init();
                break;
            case "add":
                MyUtils.validateOperands(args, 2);
                Repository.checkWorkingDirectory();
                Repository.add(args[1]);
                break;
            case "commit":
                MyUtils.validateOperands(args, 2);
                Repository.checkWorkingDirectory();
                Repository.commit(args[1]);
                break;
            case "rm":
                MyUtils.validateOperands(args, 2);
                Repository.checkWorkingDirectory();
                Repository.rm(args[1]);
                break;
            default:
                MyUtils.exit("No command with that name exists.");
        }
    }
}
