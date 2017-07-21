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

import aprel.db.beans.FileBean;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Aprel
 */
public class Reclaimer {
    
    public static final String OPTION_DRY_RUN = "n";
    public static final String OPTION_KEEP = "k";
    
    private final ArchiveDatabase db;
    private final List<FileBean> eligibleForDeletion;
    
    public Reclaimer(ArchiveDatabase database) {
        db = database;
        eligibleForDeletion = new ArrayList<>(db.getQueryObject().getAllVerifiedFilesOnDisc());
    }
    
    public List<FileBean> getFilesEligibleForDeletion() {
        return eligibleForDeletion;
    }
    
    public void exclude(String absolutePath) {
        List<FileBean> toRemove = eligibleForDeletion.stream().filter(
                f -> f.getLocalStoragePath().startsWith(absolutePath))
                .collect(Collectors.toList());
        eligibleForDeletion.removeAll(toRemove);
    }
    
    public void delete() {
        eligibleForDeletion.forEach(f -> {
            File local = new File(f.getLocalStoragePath());
            if(!local.exists())
                throw new IllegalStateException("No file at " + f.getLocalStoragePath());
            System.out.println("Deleting " + local.toString());
            if(local.delete())
                db.getInsertObject().updateNoLongerOnLocalStorage(f);
            else
                throw new IllegalStateException("File could not be deleted " + 
                        local.toString());
        });
    }
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_DRY_RUN).longOpt("dry-run")
                .desc("Do not delete any files but instead print which files "
                        + "would have been deleted").numberOfArgs(0).build());
        options.addOption(Option.builder(OPTION_KEEP).longOpt("keep").desc(
                "Skip deletion of files in this directory. (Useful for maintain"
                        + "ing a directory that stores files retrieved from the archive.)"
                        + " Must be an absolute path.")
                .numberOfArgs(1).build());
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(Verifier.class.getSimpleName(), options);

            System.exit(1);
            return;
        }
        final String excludePath = cmd.getOptionValue(OPTION_KEEP);
        if(excludePath != null && !excludePath.startsWith("/")) // TODO: This is incompatible with Windows
            throw new IllegalArgumentException("Not an absolute path: " + excludePath);
        
        ArchiveDatabase db = ArchiveDatabase.createDefaultDatabase();
        Reclaimer reclaimer = new Reclaimer(db);
        if(excludePath != null) {
            reclaimer.exclude(excludePath);
        }
        if(cmd.hasOption(OPTION_DRY_RUN)) {
            System.out.println("Would have deleted:");
            reclaimer.getFilesEligibleForDeletion().forEach(f -> {
                System.out.println(f.getFilename() + ": " + f.getLocalStoragePath());
            });
        }
        else {
            reclaimer.delete();
        }
        db.close();
    }
}
