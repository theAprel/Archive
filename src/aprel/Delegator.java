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

import aprel.optical.Isoifier;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author Aprel
 */
public class Delegator {
    
    private static enum Program {
        FILEWALKER("FileWalker", FileWalker.class),
        INSERTER("Inserter", Inserter.class),
        CONSOLE("Console", UserInterface.class),
        ISOIFY("Isoify", Isoifier.class),
        VERIFY("Verify", Verifier.class),
        RECLAIM("Reclaim", Reclaimer.class);
        
        private final String consoleCommand;
        private final Class<?> programClass;
        
        private Program(String consoleCommand, Class<?> programClass) {
            this.consoleCommand = consoleCommand;
            this.programClass = programClass;
        }

        public String getConsoleCommand() {
            return consoleCommand;
        }

        public Class<?> getProgramClass() {
            return programClass;
        }
    }
    
    public static void main(String[] args) throws Exception {
        if(args == null || args.length < 1) {
            printHelp();
            return;
        }
        final String[] passArgs = new String[args.length-1];
        System.arraycopy(args, 1, passArgs, 0, args.length-1);
        final Program called = Arrays.stream(Program.values()).filter(p -> 
                p.getConsoleCommand().equals(args[0])).findAny().orElse(null);
        if(called == null) {
            System.err.println(args[0] + " is not recognized");
            printHelp();
            return;
        }
        switch(called) {
            case FILEWALKER: FileWalker.main(passArgs); break;
            case INSERTER: Inserter.main(passArgs); break;
            case CONSOLE: UserInterface.main(passArgs); break;
            case ISOIFY: Isoifier.main(passArgs); break;
            case VERIFY: Verifier.main(passArgs); break;
            case RECLAIM: Reclaimer.main(passArgs); break;
        }
    }
    
    private static void printHelp() {
        System.out.println("Archive.jar: Fully-featured backup suite");
        System.out.println("Subprograms: " + Arrays.stream(Program.values())
                .map(Program::getConsoleCommand).collect(Collectors.joining(", ")));
        System.out.println();
        System.out.println("Workflow:");
        System.out.println();
        System.out.println(Program.FILEWALKER.getConsoleCommand());
        System.out.println("Descends into directories and prepares a METADATA.xml file with corresponding file metadata");
        System.out.println();
        System.out.println(Program.INSERTER.getConsoleCommand());
        System.out.println("Reads METADATA.xml and inserts files into the database");
        System.out.println();
        System.out.println(Program.ISOIFY.getConsoleCommand());
        System.out.println("Queries database for files not yet on optical media and prepares burnable images containing these files");
        System.out.println();
        System.out.println("(this is where the user burns the images to optical media)");
        System.out.println();
        System.out.println(Program.VERIFY.getConsoleCommand());
        System.out.println("Performs MD5-checksum verification of the files on optical media and marks the files for local deletion in the database upon successful verification; this step need not be executed on the machine that stores the local copies of the files");
        System.out.println();
        System.out.println(Program.RECLAIM.getConsoleCommand());
        System.out.println("Deletes files from local storage that have been successfully verified on optical media");
        System.out.println();
        System.out.println();
        System.out.println("Utilities:");
        System.out.println();
        System.out.println(Program.CONSOLE.getConsoleCommand());
        System.out.println("Console-based UI");
    }
}
