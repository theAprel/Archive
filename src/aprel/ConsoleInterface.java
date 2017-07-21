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

import aprel.db.beans.DbFile;
import aprel.db.beans.DirectoryBean;
import aprel.db.beans.DirectoryStructure;
import aprel.db.beans.FileBean;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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

/**
 *
 * @author Aprel
 */
public class ConsoleInterface {
    private final LinkedList<DirectoryStructure> dirPath;
    private final DirectoryBean root;
    private final ArchiveDatabase db;
    private final boolean useConsole;
    private final LineReader reader;
    private final DbCompleter filesCompleter = new DbCompleter();
    private static final String ILLEGAL_NUMBER_OF_ARGUMENTS = "Illegal number of arguments";
    
    public static final String[] COMMANDS = new String[] {
        "cd", "exit", "rmdir", "ls", "mkdir", "mv", "rename"
    };
    public static final String[] DIRECTORY_COMMANDS = new String[] {
        "cd", "rmdir", "mv", "rename"
    };
    public static final String[] FILE_COMMANDS = new String[] {
        "mv", "rename"
    };
    
    public ConsoleInterface(DirectoryBean root, ArchiveDatabase database, boolean useConsole) throws IOException {
        this.root = root;
        db = database;
        dirPath = new LinkedList<>();
        dirPath.add(new DirectoryStructure(root, db));
        this.useConsole = useConsole;
        if(useConsole) {
            updateCompleters();
            Terminal term = TerminalBuilder.builder().system(true).build();
            reader = LineReaderBuilder.builder()
                    .terminal(term)
                    .appName("Archive")
                    .completer(filesCompleter)
                    .build();
        }
        else
            reader = null;
    }
    
    public ConsoleInterface(DirectoryBean root, ArchiveDatabase database) throws IOException {
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
                final String[] parts = line.split(" ");
                cmd = parts[0];
                switch(cmd) {
                    case "cd":
                        if(parts.length != 2) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        System.out.print(cd(parts[1]));
                        break;
                    case "exit": break;
                    case "ls":
                        if(parts.length != 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        ls();
                        break;
                    case "mkdir":
                        if(parts.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        mkdir(Arrays.copyOfRange(parts, 1, parts.length));
                        break;
                    case "rmdir":
                        if(parts.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        rmdir(Arrays.copyOfRange(parts, 1, parts.length));
                        break;
                    case "rename":
                        if(parts.length != 3) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        if(!rename(parts[1], parts[2]))
                            System.out.println("There is already a file by that name.");
                        break;
                    default: System.out.println("Bad command.");
                }
            }
        }
        catch(UserInterruptException interrupted) {
            System.out.println("Closing...");
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
    
    public void mkdir(String[] names) {
        DirectoryStructure thisDir = dirPath.getLast();
        for (String name1 : names) {
            String name = name1.trim();
            if(name.length() == 0) continue;
            DirectoryBean result = thisDir.createDirectory(name);
            filesCompleter.add(result);
        }
        updateCompleters();
    }
    
    public void rmdir(String[] names) {
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
        ConsoleInterface cli = new ConsoleInterface(selectedCatalog, database);
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
        
        public DirectoryBean getDirectoryByName(String name) {
            return directoryNames.get(name);
        }
        
        public FileBean getFileByName(String name) {
            return filenames.get(name);
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
