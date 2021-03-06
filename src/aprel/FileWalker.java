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
import aprel.db.beans.FilesRootContainer;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Aprel
 */
public class FileWalker implements FileVisitor<Path> {
    
    private final Path base;
    private static boolean useFFprobe;
    private static boolean noRecursion;
    private static boolean doMd5;
    private static Map<String, String> md5Map = null;
    private static final List<FileBean> fileBeans = new ArrayList<>();
    private static final Logger LOG = LoggerFactory.getLogger(FileWalker.class);
    
    private static final String OPTION_PATH = "p";
    private static final String OPTION_NO_FFPROBE = "no-ffprobe";
    private static final String OPTION_NO_RECURSION = "no-recursion";
    private static final String OPTION_NO_MD5 = "no-md5";
    private static final String OPTION_MD5_FILE = "m";
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_PATH).required().longOpt("path")
                .desc("root directory from which to traverse files").numberOfArgs(1).build());
        options.addOption(Option.builder().longOpt(OPTION_NO_FFPROBE).numberOfArgs(0)
                .desc("do not generate media metadata for .wtv files").build());
        options.addOption(Option.builder().longOpt(OPTION_NO_RECURSION).numberOfArgs(0)
                .desc("do not enter any directory besides root directory").build());
        options.addOption(Option.builder().longOpt(OPTION_NO_MD5).numberOfArgs(0)
                .desc("do not generate MD5 checksums of files").build());
        options.addOption(Option.builder(OPTION_MD5_FILE).longOpt("md5-file")
                .desc("import MD5 checksums from file. Files not listed in the "
                        + "MD5-checksum file will be generated unless --" + OPTION_NO_MD5
                + " is set").numberOfArgs(1).build());
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(FileWalker.class.getSimpleName(), options);

            System.exit(1);
            return;
        }
        useFFprobe = !cmd.hasOption(OPTION_NO_FFPROBE);
        noRecursion = cmd.hasOption(OPTION_NO_RECURSION);
        doMd5 = !cmd.hasOption(OPTION_NO_MD5);
        if(cmd.hasOption(OPTION_MD5_FILE)) {
            md5Map = new HashMap<>();
            String md5File = cmd.getOptionValue(OPTION_MD5_FILE);
            BufferedReader reader = new BufferedReader(new FileReader(md5File));
            String line;
            while((line = reader.readLine()) != null) {
                if(line.trim().length() == 0)
                    continue;
                String[] parts = line.split("  ", 2);
                String file = parts[1];
                String checksum = parts[0];
                md5Map.put(file, checksum);
            }
            reader.close();
        }
        if(doMd5)
            System.out.println("MD5 hash is slow; consider using another program for hashing.");
        Path p = Paths.get(cmd.getOptionValue(OPTION_PATH));
        String outFileLocation = p.toString()+ "/METADATA.xml";
        File outFile = new File(outFileLocation);
        if(outFile.exists()) {
            System.err.println(outFileLocation + " already exists.\nDelete file"
                    + " to before running this program.");
            System.exit(-1);
        }
        
        FileWalker fw = new FileWalker(p);
        Files.walkFileTree(p, fw);
        
        JAXBContext jaxbContext = JAXBContext.newInstance(FilesRootContainer.class);
        Marshaller marsh = jaxbContext.createMarshaller();
        JAXBSource jsource = new JAXBSource(marsh, new FilesRootContainer(fileBeans));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StreamResult result = new StreamResult(outFile);
        transformer.transform(jsource, result);
    }
    
    public FileWalker(Path base) {
        this.base = base;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(noRecursion && !dir.equals(base))
            return FileVisitResult.SKIP_SUBTREE;
        final Path relative = base.relativize(dir);
        LOG.info("Entering directory " + relative);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final Path relative = base.relativize(file);
        LOG.info("Found file " + relative);
        FileBean bean = new FileBean();
        bean.setPath(relative.toString());
        fileBeans.add(bean);
        long size = Files.size(file);
        bean.setSize(size);
        String md5String = null;
        if(md5Map != null) {
            md5String = md5Map.get(relative.toString());
        }
        if(doMd5 && md5String == null) {
            @SuppressWarnings("deprecation")
            HashFunction md5 = Hashing.md5();
            HashCode hc = com.google.common.io.Files.asByteSource(file.toFile()).hash(md5);
            md5String = hc.toString();
        }
        bean.setMd5(md5String);
        if(md5String == null) {
            LOG.warn(relative + " has not been assigned a checksum.");
        }
        if(useFFprobe && file.toString().endsWith(".wtv")) {
            ProcessBuilder pb = new ProcessBuilder("ffprobe", file.toString());
            Process p = pb.start();
            try {
                p.waitFor();
            }
            catch(InterruptedException ex) {
                LOG.error("Interrupted while waiting for process. How could this happen?", ex);
            }
            if(p.exitValue() != 0) {
                LOG.error("Error calling ffprobe for file " + file);
            }
            else {
                BufferedReader ffprobe = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line = ffprobe.readLine())!=null) {
                    String[] parts = line.split(":", 2);
                    String possibleKey = parts[0].trim();
                    WtvMetadata corresponding = WtvMetadata.getFromWtvMetadataKey(possibleKey);
                    if(corresponding != null) {
                        String value = parts[1].trim();
                        if(!bean.hasMediaData()) {
                            bean.setMedia(new FileBean.MediaMetadata());
                        }
                        FileBean.MediaMetadata media = bean.getMedia();
                        if(corresponding == WtvMetadata.DURATION) {
                            //ffprobe/WTV has two duration values: one as
                            //100*nanoseconds and another as hrs:mins:secs.fraction
                            String durationParts[] = value.split(",");
                            if(durationParts.length > 1) { //is hrs:mins:secs
                                media.setDuration(durationParts[0]);
                            }
                            else {
                                media.setDuration100Nanos(value);
                            }
                            continue;
                        }
                        switch(corresponding) {
                            case TITLE: media.setTitle(value); break;
                            case SUBTITLE: media.setSubtitle(value); break;
                            case DESCRIPTION: media.setDescription(value); break;
                            case CHANNEL: media.setChannel(value); break;
                            case ORIGINAL_BROADCAST_DATETIME: 
                                //convert into MySQL datetime
                                value = value.replace("T", " ").replace("Z", "");
                                media.setOriginalBroadcast(value);
                                break;
                            case ORIGINAL_RUNTIME: media.setOriginalRuntime(value); break;
                            default: LOG.error("Misprocessed a WtvMetadata enum: " + corresponding); break;
                        }
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        LOG.warn("Failed to visit " + base.relativize(file), exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        LOG.info("Exiting directory " + base.relativize(dir));
        return FileVisitResult.CONTINUE;
    }
}
