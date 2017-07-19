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
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 *
 * @author Aprel
 */
public class ConsoleInterface {
    private final LinkedList<DirectoryStructure> dirPath;
    private final DirectoryBean catalog;
    private final Map<String,DirectoryBean> directoryNames;
    private final ArchiveDatabase db;
    private final LineReader reader;
    private final DbCompleter filesCompleter = new DbCompleter();
    private static final String ILLEGAL_NUMBER_OF_ARGUMENTS = "Illegal number of arguments";
    
    public ConsoleInterface(DirectoryBean catalog, ArchiveDatabase database) throws IOException {
        if(catalog.getDirParentId() != null)
            throw new IllegalArgumentException("Not a catalog: " + catalog);
        this.catalog = catalog;
        db = database;
        dirPath = new LinkedList<>();
        directoryNames = new HashMap<>();
        dirPath.add(new DirectoryStructure(catalog, db));
        updateCompleters();
        Terminal term = TerminalBuilder.builder().system(true).build();
        StringsCompleter commands = new StringsCompleter("cd", "exit", "rmdir", "ls", "mkdir");
        reader = LineReaderBuilder.builder()
                .terminal(term)
                .appName("Archive")
                .completer(new ArgumentCompleter(commands, filesCompleter))
                .build();
    }
    
    public void initiate() {
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
                        System.out.print(changeDirectories(parts[1]));
                        break;
                    case "exit": break;
                    case "ls":
                        if(parts.length != 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        List<DbFile> files = dirPath.getLast().getFiles();
                        files.sort((DbFile o1, DbFile o2) -> {
                            return o1.getName().compareTo(o2.getName());
                        });
                        files.forEach(f -> {
                            System.out.print(f instanceof DirectoryBean ? "DIR  " : "FIL  ");
                            System.out.println(f.getName());
                        });
                        break;
                    case "mkdir":
                        if(parts.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        DirectoryStructure thisDir = dirPath.getLast();
                        for(int i = 1; i < parts.length; i++) {
                            String name = parts[i].trim();
                            if(name.length() == 0) continue;
                            DirectoryBean result = thisDir.createDirectory(name);
                            directoryNames.put(name, result);
                        }
                        updateCompleters();
                        break;
                    case "rmdir":
                        if(parts.length <= 1) {
                            System.out.println(ILLEGAL_NUMBER_OF_ARGUMENTS);
                            break;
                        }
                        for(int i = 1; i < parts.length; i++) {
                            String name = parts[i];
                            DirectoryBean dir = directoryNames.get(name);
                            if(dir == null) {
                                System.out.println("No directory by name " + name);
                                continue;
                            }
                            boolean deleted = dirPath.getLast().deleteChildDirectory(dir);
                            if(!deleted)
                                System.out.println("Directory not empty. Not deleting " + name);
                        }
                        updateCompleters();
                        break;
                    default: System.out.println("Bad command.");
                }
            }
        }
        catch(UserInterruptException interrupted) {
            System.out.println("Closing...");
        }
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
            cli.initiate();
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
    private String changeDirectories(String enter) {
        if(enter == null)
            return "Cannot enter a null\n";
        if(enter.equals("..")) {
            if(dirPath.getLast() == dirPath.getFirst())
                return "Already at the root\n";
            dirPath.removeLast();
        }
        else {
            DirectoryBean changeTo = directoryNames.get(enter);
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
        directoryNames.clear();
        filesCompleter.clearCandidates();
        ds.getFiles().stream().forEach(f -> {
            final String name = f.getName();
            filesCompleter.addCandidate(name);
            if(f instanceof DirectoryBean)
                directoryNames.put(name, (DirectoryBean) f);
        });
    }
    
    private static class DbCompleter extends StringsCompleter {

        public DbCompleter() {
            super();
        }
        
        public void addCandidate(String c) {
            candidates.add(new Candidate(c));
        }
        
        public void clearCandidates() {
            candidates.clear();
        }
        
    }
}
