/*
 * Copyright (C) 2017 Aprel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package aprel;

import aprel.db.beans.CatalogDoesNotExistException;
import aprel.db.beans.DbFile;
import aprel.db.beans.Directories;
import aprel.db.beans.DirectoryBean;
import aprel.db.beans.DirectoryStructure;
import aprel.db.beans.FileBean;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import aprel.db.beans.FileBean.MediaMetadata;

/**
 *
 * @author Aprel
 */
public class UserInterface {
    private final LinkedList<DirectoryStructure> dirPath;
    private final DirectoryBean catalog;
    private final ArchiveDatabase db;
    private final boolean useConsole;
    private final LineReader reader;
    private final DbCompleter filesCompleter = new DbCompleter();
    private static final String ILLEGAL_NUMBER_OF_ARGUMENTS = "Illegal number of arguments";
    
    public static final String[] COMMANDS = new String[] {
        "cd", "exit", "rmdir", "ls", "mkdir", "mv", "rename", "metadata"
    };
    public static final String[] DIRECTORY_COMMANDS = new String[] {
        "cd", "rmdir", "mv", "rename"
    };
    public static final String[] FILE_COMMANDS = new String[] {
        "mv", "rename", "metadata"
    };
    
    public UserInterface(DirectoryBean catalog, ArchiveDatabase database, boolean useConsole) throws IOException {
        this.catalog = catalog;
        db = database;
        dirPath = new LinkedList<>();
        dirPath.add(new DirectoryStructure(catalog, db));
        this.useConsole = useConsole;
        if(useConsole) {
            updateCompleters();
            Terminal term = TerminalBuilder.builder().system(true).build();
            reader = LineReaderBuilder.builder()
                    .terminal(term)
                    .appName("Archive")
                    .completer(filesCompleter)
                    .build();
            reader.unsetOpt(LineReader.Option.GLOB_COMPLETE);
        }
        else
            reader = null;
    }
    
    public UserInterface(DirectoryBean root, ArchiveDatabase database) throws IOException {
        this(root, database, true);
    }
    
    public void readFromTerminal() {
        if(!useConsole)
            throw new IllegalStateException("UserInterface not initialized with useConsole = true");
        String cmd = "";
        String line;
        try {
            while(!cmd.equals("exit")) {
                line = reader.readLine(dirPath.stream()
                        .map(DirectoryStructure::getName).collect(Collectors.joining("/")) + ">");
                Arguments args;
                try {
                    args = new Arguments(line);
                }
                catch(IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
                    continue;
                }
                cmd = args.get(0);
                switch(cmd) {
                    case "cd":
                        if(args.length != 2) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        System.out.print(cd(args.get(1)));
                        break;
                    case "exit": break;
                    case "ls":
                        if(args.length != 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        ls();
                        break;
                    case "mkdir":
                        if(args.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        mkdir(args.getArgumentsAfterCommand());
                        break;
                    case "rmdir":
                        if(args.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        rmdir(args.getArgumentsAfterCommand());
                        break;
                    case "rename":
                        if(args.length != 3) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        try {
                            if(!rename(args.get(1), args.get(2)))
                                System.out.println("There is already a file by that name.");
                        }
                        catch(IllegalArgumentException ex) {
                            System.out.println("Bad argument: " + ex.getMessage());
                        }
                        break;
                    case "mv":
                        if(args.length != 3) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        if(!mv(filesCompleter.globForNames(args.get(1)), args.get(2))) {
                            System.out.println("Could not move file(s).");
                        }
                        break;
                    case "metadata":
                        if(args.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        args.getArgumentsAfterCommand().stream()
                                .map(arg -> filesCompleter.globForFiles(arg))
                                .flatMap(l -> l.stream()).filter(f -> f instanceof FileBean)
                                .map(f -> (FileBean) f).filter(f -> f.queryMetadata(db))
                                .forEach(f -> {
                                    System.out.println(f.getFilename()+":");
                                    System.out.println(f.getMedia().prettyPrint());
                                    System.out.println();
                                });
                        
                        break;
                    case "printargs": //debugging command for arg parser
                        System.out.println("Command:" + args.get(0));
                        System.out.println("Arguments:\n" + args.getArgumentsAfterCommand()
                                .stream().collect(Collectors.joining("\n")));
                        break;
                    default: System.out.println("Bad command.");
                }
            }
        }
        catch(UserInterruptException interrupted) {
            System.out.println("Closing...");
        }
    }
    
    private static class Arguments {
        private final String line;
        private final List<String> arguments;
        public final int length;
        
        public Arguments(String line) {
            this.line = line;
            arguments = new ArrayList<>();
            String[] words = line.split(" ");
            for(int i = 0; i < words.length; i++) {
                String word = words[i];
                if(word.startsWith("\"")) {
                    StringBuilder builder = new StringBuilder(word.substring(1));
                    String nextWord;
                    do {
                        try {
                            nextWord = words[++i];
                        }
                        catch(ArrayIndexOutOfBoundsException ex) {
                            throw new IllegalArgumentException("Bad quote syntax", ex);
                        }
                        builder.append(" ").append(nextWord);
                    } while(!nextWord.endsWith("\""));
                    //remove trailing quote
                    arguments.add(builder.substring(0, builder.length() - 1));
                }
                else if(word.contains("\""))
                    throw new IllegalArgumentException("Bad quote syntax");
                else arguments.add(word);
            }
            length = arguments.size();
        }
        
        public String get(int index) {
            return arguments.get(index);
        }
        
        public List<String> getArgumentsAfterCommand() {
            return new ArrayList<>(arguments.subList(1, length));
        }

    }
    
    public void ls() {
        List<DbFile> files = dirPath.getLast().getFiles();
        files.sort((DbFile o1, DbFile o2) -> {
            return o1.getName().compareTo(o2.getName());
        });
        files.forEach(f -> {
            System.out.print(f instanceof DirectoryBean ? "DIR  " : "FIL  ");
            System.out.println(f.getName());
        });
    }
    
    public void mkdir(List<String> names) {
        DirectoryStructure thisDir = dirPath.getLast();
        for (String name1 : names) {
            String name = name1.trim();
            if(name.length() == 0) continue;
            DirectoryBean result = thisDir.createDirectory(name);
            filesCompleter.add(result);
        }
        updateCompleters();
    }
    
    public void rmdir(List<String> names) {
        for(String name : names) {
            DirectoryBean dir = filesCompleter.getDirectoryByName(name);
            if(dir == null) {
                System.out.println("No directory by name " + name);
                continue;
            }
            boolean deleted = dirPath.getLast().deleteChildDirectory(dir);
            if(!deleted)
                System.out.println("Directory not empty. Not deleting " + name);
        }
        updateCompleters();
    }
    
    public boolean mv(List<String> files, String pathToNewParentFromRoot) {
        Directories directoriesPath;
        try {
        directoriesPath = new Directories(catalog.getDirName(), 
                pathToNewParentFromRoot, db);
        }
        catch(CatalogDoesNotExistException ex) {
            throw new IllegalStateException(ex);
        }
        DirectoryBean newBeanParent = directoriesPath.getPath().getLast();
        DirectoryStructure newParent = new DirectoryStructure(newBeanParent, db);
        List<DbFile> fileAndDirBeans = files.stream().map(f -> 
                filesCompleter.getByName(f)).collect(Collectors.toList());
        List<DbFile> cannotAccept = fileAndDirBeans.stream().filter(
                f -> !newParent.canAccept(f)).collect(Collectors.toList());
        if(!cannotAccept.isEmpty()) {
            System.out.println("Cannot move because of duplicates of the following"
                    + " filenames in the new parent directory:\n" + cannotAccept.stream()
                            .map(DbFile::getName).collect(Collectors.joining("\n")));
            return false;
        }
        else {
            DirectoryStructure thisDir = dirPath.getLast();
            thisDir.moveTo(newParent, fileAndDirBeans);
            return true;
        }
    }
    
    public boolean rename(String file, String newName) {
        DbFile f = filesCompleter.getFileByName(file);
        if(f == null) f = filesCompleter.getDirectoryByName(file);
        DirectoryStructure thisDir = dirPath.getLast();
        boolean toReturn = thisDir.rename(f, newName);
        if(toReturn) updateCompleters();
        return toReturn;
    }
    
    public static void main(String[] args) throws Exception {
        ArchiveDatabase database = ArchiveDatabase.createDangerousDefaultDatabase();
        List<DirectoryBean> catalogs = database.getCatalogs();
        if(args == null || args.length != 1) {
            System.err.println("Illegal command line arguments. Must specify catalog to enter.");
            System.out.println("Catalogs:");
            System.out.println(catalogs.stream().map(DirectoryBean::getDirName)
                    .collect(Collectors.joining(", ")));
            return;
        }
        DirectoryBean selectedCatalog = catalogs.stream().filter(c -> c.getDirName()
                .equals(args[0])).findAny().orElse(null);
        if(selectedCatalog == null) {
            System.err.println("I don't know any catalog by that name.");
            return;
        }
        UserInterface cli = new UserInterface(selectedCatalog, database);
        try {
            cli.readFromTerminal();
        }
        finally {
            database.close();
        }
    }
    
    /**
     * 
     * @param enter
     * @return whether the change was successful
     */
    public String cd(String enter) {
        if(enter == null)
            return "Cannot enter a null\n";
        if(enter.equals("..")) {
            if(dirPath.getLast() == dirPath.getFirst())
                return "Already at the root\n";
            dirPath.removeLast();
        }
        else {
            DirectoryBean changeTo = filesCompleter.getDirectoryByName(enter);
            if(changeTo == null)
                return "Directory \"" + enter + "\" does not exist.\n";
            DirectoryStructure ds = new DirectoryStructure(changeTo, db);
            dirPath.add(ds);
        }
        updateCompleters();
        return "";
    }
    
    private void updateCompleters() {
        DirectoryStructure ds = dirPath.getLast();
        filesCompleter.update(ds.getFiles());
    }
    
    private static class DbCompleter implements Completer {
        
        private final Map<String,DirectoryBean> directoryNames;
        private final Map<String,FileBean> filenames;
        
        public DbCompleter() {
            directoryNames = new HashMap<>();
            filenames = new HashMap<>();
        }
        
        public void add(DbFile f) {
            if(f instanceof FileBean) filenames.put(f.getName(), (FileBean) f);
            else if(f instanceof DirectoryBean) directoryNames.put(f.getName(), (DirectoryBean) f);
            else throw new IllegalArgumentException("Unrecognized class: " + f.getClass());
        }
        
        public void clear() {
            directoryNames.clear();
            filenames.clear();
        }
        
        public void update(Collection<DbFile> files) {
            clear();
            files.forEach(f -> add(f));
        }
        
        /**
         * 
         * @param nameWithAsterisk
         * @return an ordered-alphabetically list containing all files and directories 
         * whose names match the String provided. If the String does not contain 
         * an asterisk, returns a singleton list containing the file by that exact 
         * name, or an empty list otherwise.
         */
        public List<DbFile> globForFiles(String nameWithAsterisk) {
            if(!nameWithAsterisk.contains("*")) {
                DbFile file = getByName(nameWithAsterisk);
                if(file == null) return new ArrayList<>();
                else return Collections.singletonList(file);
            }
            List<DbFile> list = new ArrayList<>();
            String prefix = nameWithAsterisk.substring(0, nameWithAsterisk.indexOf("*"));
            Map<String, DbFile> allFiles = new HashMap<>(filenames);
            allFiles.putAll(directoryNames);
            allFiles.forEach((name, file) -> {
                if(name.startsWith(prefix))
                    list.add(file);
            });
            list.sort((f1, f2) -> {return f1.getName().compareTo(f2.getName());});
            return list;
        }
        
        public List<String> globForNames(String nameWithAsterisk) {
            return globForFiles(nameWithAsterisk).stream().map(DbFile::getName)
                    .collect(Collectors.toList());
        }
        
        public DirectoryBean getDirectoryByName(String name) {
            return directoryNames.get(name);
        }
        
        public FileBean getFileByName(String name) {
            return filenames.get(name);
        }
        
        public DbFile getByName(String name) {
            DbFile file = filenames.get(name);
            if(file != null) return file;
            else return directoryNames.get(name);
        }

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            List<String> words = line.words();
            if(words == null) return;
            final int wordIndex = line.wordIndex();
            if(wordIndex == 0) {
                candidates.addAll(Arrays.stream(COMMANDS).map(
                        com -> new Candidate(com)).collect(Collectors.toList()));
                return;
            }
            else {
                final String command = words.get(0);
                Set<String> cands = new HashSet<>();
                if(Arrays.asList(DIRECTORY_COMMANDS).contains(command)) {
                    cands.addAll(directoryNames.keySet());
                }
                if(Arrays.asList(FILE_COMMANDS).contains(command)) {
                    cands.addAll(filenames.keySet());
                }
                candidates.addAll(cands.stream().map(c -> new Candidate(c))
                        .collect(Collectors.toSet()));
            }
        }

    }
}
