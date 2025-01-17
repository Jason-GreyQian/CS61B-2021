package gitlet;


/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author GreyQian
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // what if args is empty?
        if (args.length < 1) {
            MyUtils.exit("Please enter a command.");
        }

        String firstArg = args[0];
        switch (firstArg) {
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
            case "log":
                MyUtils.validateOperands(args, 1);
                Repository.checkWorkingDirectory();
                Repository.log();
                break;
            case "global-log":
                MyUtils.validateOperands(args, 1);
                Repository.checkWorkingDirectory();
                Repository.globalLog();
                break;
            case "find":
                MyUtils.validateOperands(args, 2);
                Repository.checkWorkingDirectory();
                Repository.find(args[1]);
                break;
            case "status":
                MyUtils.validateOperands(args, 1);
                Repository.checkWorkingDirectory();
                Repository.status();
                break;
            case "checkout":
                Repository.checkWorkingDirectory();
                Repository.checkout(args);
                break;
            case "branch":
                Repository.checkWorkingDirectory();
                MyUtils.validateOperands(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.checkWorkingDirectory();
                MyUtils.validateOperands(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                Repository.checkWorkingDirectory();
                MyUtils.validateOperands(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.checkWorkingDirectory();
                MyUtils.validateOperands(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                MyUtils.exit("No command with that name exists.");
        }
    }
}
