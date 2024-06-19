import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.Scanner;

class Parser {
    String commandName;
    String[] args;

    public boolean parse(String input) {
        //inputList consists of our input but separated each by a space and stored in an array of strings(split function).
        //if input="echo Improving the whole universe", then inputList will be{"echo","Improving","the","whole","universe"}
        String[] inputList = input.split(" ");
        this.commandName = getCommand(inputList);
        this.args = getArgs(inputList);
        return true;
    }

    public String[] getArgs(String[] inputList) {
        try {
            //we create a dynamic array of strings to store our args in.
            Vector<String> v = new Vector<>();
            //we start from index 1 as index 0 already a commandName.
            int index = 1;
            if (inputList[1].equals("-r")) { // if second element in the original input array is an option, we don't count it as arg so we start from index 2 in inputList
                index = 2;
            }
            for (; index < inputList.length; index++) {
                //we add every element as arg
                v.add(inputList[index]);
            }
            //conversion of vector to array with the same size of vector to convert all elements without losing any element.
            return v.toArray(new String[v.size()]);
        } catch (Exception e) { // throws an exception when its size is zero [ no elements like pwd ] then we return an empty array.
            return new String[0];
        }
    }

    public String getCommand(String[] inputList) {
        try {
            //we make a check on second element in array, we have a probability that second element be an option (-r in ls and cp)
            //if it's true we add it to the command.
            if (inputList[1].equals("-r")) {
                return inputList[0] + " " + inputList[1];
            } else { // second element is a normal input so, we return first element only as a command.
                return inputList[0];
            }
        }
        //it may throw exception if we tried to access inputList[1] and inputList consists of only one element ex: pwd
        //so we return inputList[0] as command = pwd;
        catch (Exception e) {
            return inputList[0];
        }
    }

}

public class Terminal{

    Parser parser;
    Terminal(){
        parser = new Parser();
        parser.commandName="";
        parser.args= new String[]{};
    }

    public String echo(String input) {
        return input;
    }
    public String pwd() {
        Path currentDir = Paths.get("").toAbsolutePath();
        return currentDir.toString();
    }

    public void cd(String[] args) {
        if (args.length == 0) {  // no argument given, go to home directory
            Path homeDir = Paths.get(System.getProperty("user.home"));
            System.setProperty("user.dir", homeDir.toString());
        } else {  // argument given, try to change directory
            Path dir = Paths.get(args[0]);
            if (Files.exists(dir) && Files.isDirectory(dir)) {
                System.setProperty("user.dir", dir.toAbsolutePath().toString());
            } else {
                System.err.println("Invalid directory: " + args[0]);
            }
        }
    }

    public void ls(String[] args) {
        Path dir;
        if (args.length == 0) {  // no argument given, use current working directory
            dir = Paths.get(System.getProperty("user.dir"));
        } else {  // argument given, use the specified directory
            dir = Paths.get(args[0]);
        }
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (var stream = Files.list(dir)) {
                stream.forEach(System.out::println);  // print each file or directory
            } catch (IOException e) {
                System.err.println("Error listing directory: " + e.getMessage());
            }
        } else {
            System.err.println("Invalid directory: " + dir.toString());
        }
    }

    public void lsr (String[] args) {
        // Check if argument(s) are provided
        if (args.length > 0) {
            // Display error message and return
            System.err.println("ls -r does not take arguments.");
            return;
        }

        // Get the current directory
        File dir = new File(".");

        // Check if the directory exists and is readable
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            // Display error message and return
            System.err.println("Cannot list directory.");
            return;
        }

        // Get the list of files and directories in the directory
        File[] files = dir.listFiles();

        // Sort the files in reverse order
        Arrays.sort(files, Collections.reverseOrder());

        // Print the names of the files and directories
        for (File file : files) {
            System.out.println(file.getName());
        }
    }

    public String mkdir(String[] args) {
        StringBuilder sb = new StringBuilder();

        // Check if argument(s) are provided
        if (args.length == 0) {
            // Display error message and return
            sb.append("mkdir requires an argument.\n");
            return sb.toString();
        }

        // Create StringBuilder for directories created
        StringBuilder dirsCreated = new StringBuilder();

        // Iterate through the arguments and create directories
        for (String arg : args) {
            File dir = new File(arg);
            if (dir.exists()) {
                // Display error message and continue
                sb.append("Directory " + arg + " already exists.\n");
                continue;
            }

            if (dir.mkdirs()) {
                dirsCreated.append(arg + " ");
            } else {
                // Display error message if directory creation fails
                sb.append("Failed to create directory " + arg + "\n");
            }
        }

        // Append created directories to output
        if (dirsCreated.length() > 0) {
            sb.append("Created directory " + dirsCreated.toString() + "\n");
        }

        return sb.toString();
    }

    public static void cp(String source, String destination) {
        try {
            Files.copy(Paths.get(source).toAbsolutePath(), Paths.get(destination).toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { // Throwing an exception if the source path does not exist.
            System.out.println("Paths are wrong, try another path: ");
        }

    }

    public static void cpr(String source, String destination) {
        //Initializing file object with source as path.
        File sourceFile= new File(source);
        //Initializing file object with destination as path.
        File destinationFile=new File(destination);
        if ( sourceFile.isDirectory()) {
            // True if destination does not have a directory with the same name So, we Create one.
            if (!destinationFile.exists()) {
                destinationFile.mkdir();

            }
            // files is an array that consists of all objects inside our directory.
            String[] files = sourceFile.list();
            for (int i = 0; i < files.length; i++) {
    // we make a recursive call with Parent directory ex:"D:\\Folder" and then we concatenate child path (either Folder or file) so we make call with(D:\\Folder\\childfile) untill we reach a file not directory.
                cpr(sourceFile.toString()+"\\"+files[i], destinationFile.toString()+"\\"+files[i]);
            }
        }
        //else: this sourcefile we are pointing to is normal file not a directory so, we perform straightforward files copying.
        else{
            cp(sourceFile.toString(),destinationFile.toString());
        }
    }

    public static void rmdir() {
        //Getting our current path and storing it in fullPath.
        String fullPath = Paths.get("").toAbsolutePath().toString();
        //Initializing file with current path.
        File file = new File(fullPath);
        // we make a check on our current directory, if its length is greater than zero then directory has items and can not be deleted.
        if (file.list().length>0){
            System.out.println(file.getName()+" is not empty");
        }
        else{ // directory's length is zero and we delete it.
            file.delete();
        }
    }

    public static void rmdirPath(String fullPath){
        //Initializing file path with input string
        File current_path = new File(fullPath);
        // length of a directory represents number of elements inside it, if it's zero we can remove the directory.
        if(current_path.list().length==0) {
            current_path.delete();
        }
        else{ // directory's length is greater than zero( has elements inside).
            System.out.println(current_path.getName()+" is not empty");
        }
    }

    public void rmdirHelper(String arg){
        //depending on our arg, we compare arg to * or our arg is normal directory path.
        if (arg.equals("*")){
            rmdir();
        }
        else{ //arg is any string except * so we call the other function.
            rmdirPath(arg);
        }
    }

    public static void touch(String path)  {
        //Initializing file with input path
        File touchedFile = new File(path);
        try{
            touchedFile.createNewFile();
        }catch (Exception e){System.out.println("Error");}
    }
    public void rm(String arg) // a function for removing a specified file at the current directory
    {
        Path CurrentDir = Paths.get(pwd()).toAbsolutePath(); // pwd is the command that's returning the path of the directory we are at.
        File file = new File(CurrentDir.toString(),arg); // to get absolute path of file and check if it's exist before trying to remove.
        if (file.exists())
        {
            if (file.delete()) {
                System.out.println("File removed successfully.");
            }
            else {
                System.out.println("Unable to remove the file.");
            }
        }
        else {
            System.out.println("File does not exist.");
        }
    }

    public String wc(String arg) {
        Path currentDir = Paths.get(System.getProperty("user.dir")); // Getting the current directory path

        // Resolve the file path and create a File object
        Path filePath = currentDir.resolve(arg).toAbsolutePath();
        File file = filePath.toFile();

        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                int lines = 0;
                int words = 0;
                int characters = 0;
                String line;

                while ((line = reader.readLine()) != null) {
                    lines++;
                    characters += line.length();
                    // Splitting the line into words based on spaces
                    String[] wordsArray = line.split("\\s+"); // This pattern splits by spaces
                    words += wordsArray.length;
                }

                reader.close();
                return "Lines: " + lines + "\nWords: " + words + "\nCharacters: " + characters;
            } catch (IOException e) {
                return "Error reading the file.";
            }
        } else {
            return "File does not exist.";
        }
    }

    public String cat(String arg) {
        try {
            Path currentDir = Paths.get(pwd()).toAbsolutePath(); // Assuming pwd() returns the current directory path
            File file = new File(currentDir.toString(), arg); // getting the file directory concatenated with filename
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder content = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                reader.close();
                return content.toString();
            } else {
                return "File does not exist.";
            }
        } catch (IOException e) {
            return "Error reading the file.";
        }
    }
    public void InvalidArgs(){System.out.println("Invalid number of args");}
    public void ChooseCommandAction(){
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        boolean status = parser.parse(input);
        switch(parser.commandName){
            case "pwd":
                System.out.println(pwd());
                break;
            case "echo":
                System.out.println(echo(String.join(" ", parser.args)));
                break;
            case "ls":
                ls(parser.args);
                break;
            case "ls -r":
                lsr(parser.args);
                break;
            case "mkdir":
                mkdir(parser.args);
                break;
            case "cd":
                cd(parser.args);
                break;
            case "touch":
                if(parser.args.length==1) {
                    touch(parser.args[0]);
                }
                else{
                    InvalidArgs();
                }
                break;
            case "cp":
                if(parser.args.length==2) {
                    cp(parser.args[0], parser.args[1]);
                }
                else{
                    InvalidArgs();
                }
                break;
            case "cp -r":
                if(parser.args.length==2) {
                    cpr(parser.args[0], parser.args[1]);
                }
                else{
                    InvalidArgs();
                }
                break;
            case "rmdir":
                if(parser.args.length==1) {
                    rmdirHelper(parser.args[0]);
                }
                else{
                    InvalidArgs();
                }
                break;
            case "wc":
                if(parser.args.length==1) {
                    System.out.println(wc(parser.args[0]));// we use System.out.print because wc returning string we wanna show to user.
                }
                else{
                    InvalidArgs();
                }
                break;
            case "rm":
            if(parser.args.length==1) {
                rm(parser.args[0]);// we use System.out.print because wc returning string we wanna show to user.
            }
            else{
                InvalidArgs();
            }
                break;
            case "cat":
                if(parser.args.length==1) {
                    System.out.println(cat(parser.args[0]));// we use System.out.print because wc returning string we wanna show to user.
                }
                else{
                    InvalidArgs();
                }// we use System.out.print because cat returning the string we wanna show to user.
                break;
            case "exit":
                System.exit(0);
                break;
            default:
                System.out.println("invalid command");
        }
    }

    public static void main(String[] args){
        Terminal terminal = new Terminal();
        while(true){
            terminal.ChooseCommandAction();
        }
    }
}
