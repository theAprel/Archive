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
import aprel.jdbi.Insert;
import aprel.optical.Isoifier;
import aprel.optical.Part;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.skife.jdbi.v2.Query;

/**
 *
 * @author Aprel
 */
public class Verifier implements FileVisitor<Path> {
    private static final String OPTION_FILE_DIR = "i";
    
    private final String opticalRoot;
    private final Map<String,String> fileToMd5sum;
    private final HashFunction md5;
    private final Set<String> verifiedIds;
    private boolean hasMd5VerificationErrors = false;
    
    @SuppressWarnings("deprecation")
    private Verifier(String opticalRoot) throws IOException {
        this.opticalRoot = opticalRoot.endsWith(File.separator) ? opticalRoot 
                : opticalRoot + File.separator;
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(this.opticalRoot + 
                        Isoifier.CHECKSUMS_FILENAME)), Charset.forName("utf-8")));
        fileToMd5sum = new HashMap<>();
        String line;
        while((line = reader.readLine()) != null && line.length() > 0) {
            String[] parts = line.split("  ", 2);
            if(parts.length != 2)
                throw new IllegalArgumentException("unrecognized format of md5 file");
            String filename = parts[1];
            String checksum = parts[0];
            fileToMd5sum.put(filename, checksum);
        }
        md5 = Hashing.md5();
        verifiedIds = new HashSet<>();
    }
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_FILE_DIR).required()
                .desc("The root of the optical media").numberOfArgs(1).build());
        
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
        final String opticalDir = cmd.getOptionValue(OPTION_FILE_DIR);
        
        ArchiveDatabase db = ArchiveDatabase.createDefaultDatabase();
        Verifier v = new Verifier(opticalDir);
        Files.walkFileTree(Paths.get(opticalDir), v);
        if(v.hasVerificationErrors()) {
            System.err.println("Verification errors encountered. Database will not be updated.");
        }
        else {
            System.out.println("Verification successful. Updating database...");
            v.updateDatabase(db);
        }
        db.close();
    }
    
    public void updateDatabase(ArchiveDatabase db) {
        if(hasVerificationErrors())
            throw new IllegalStateException("Cannot update database because "
                    + "there were verification errors");
        Insert ins = db.getInsertObject();
        ins.updateVerifiedParts(verifiedIds.iterator());
        updateFilesFullyVerified(db);
    }
    
    private static void updateFilesFullyVerified(ArchiveDatabase db) {
        List<FileBean> unverifiedFiles = db.getQueryObject().getAllFilesNotMd5Verified();
        String fileIds = unverifiedFiles.stream().map(FileBean::getId)
                .collect(Collectors.joining(","));
        Query<Part> partQuery = db.getHandle().createQuery("SELECT * FROM parts WHERE parentFileId IN (" + fileIds + ")")
                .map(Part.class);
        List<Part> partsWithUnverifiedParents = partQuery.list();
        Multimap<String,Part> parentFileIdsToPartsInDb = HashMultimap.create();
        partsWithUnverifiedParents.forEach(p -> parentFileIdsToPartsInDb.put(
                p.getParentFileId(), p));
        Set<String> fullyVerifiedIds = new HashSet<>();
        parentFileIdsToPartsInDb.asMap().forEach((parent, children) -> {
            if(!children.isEmpty()) {
                final int totalInSet = children.stream().findAny().get().getTotalInSet();
                if(children.size() == totalInSet) {
                    if(children.stream().allMatch(Part::isMd5Verified)) {
                        fullyVerifiedIds.add(parent);
                    }
                }
            }
        });
        db.getInsertObject().updateFullyVerifiedFiles(fullyVerifiedIds.iterator());
    }
    
    public boolean hasVerificationErrors() {
        //second part is whether there are more files in md5 checksum file than encountered in dir
        return hasMd5VerificationErrors || verifiedIds.size() != fileToMd5sum.size();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(file.getFileName().toString().equals(Isoifier.CHECKSUMS_FILENAME))
            return FileVisitResult.CONTINUE;
        String fromChecksumFile = fileToMd5sum.get(file.getFileName().toString());
        if(fromChecksumFile == null) {
            System.err.println("No checksum in checksum file for " + file);
            return FileVisitResult.TERMINATE;
        }
        String calculatedSum = com.google.common.io.Files.asByteSource(file.toFile())
                .hash(md5).toString();
        if(fromChecksumFile.equalsIgnoreCase(calculatedSum)) {
            System.out.println(file.getFileName() + ": OK");
            verifiedIds.add(file.getFileName().toString().split(
                    Isoifier.FILENAME_ORDINAL_SEPARATOR)[0]);
            return FileVisitResult.CONTINUE;
        }
        else {
            hasMd5VerificationErrors = true;
            System.err.println("FAILED: " + file.getFileName());
            return FileVisitResult.TERMINATE;
        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("Error visiting file " + file);
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
