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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Aprel
 */
public class FileWalker implements FileVisitor<Path> {
    
    private final Path base;
    private static boolean useFFprobe;
    private static boolean noRecursion;
    private static boolean doMd5;
    private static Document doc;
    private static Element rootElement;
    
    private static final String OPTION_PATH = "p";
    private static final String OPTION_NO_FFPROBE = "no-ffprobe";
    private static final String OPTION_NO_RECURSION = "no-recursion";
    private static final String OPTION_NO_MD5 = "no-md5";
    
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
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        rootElement = doc.createElement("FILES");
        doc.appendChild(rootElement);
        
        FileWalker fw = new FileWalker(p);
        Files.walkFileTree(p, fw);
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outFile);
        transformer.transform(source, result);
    }
    
    public FileWalker(Path base) {
        this.base = base;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(noRecursion && !dir.equals(base))
            return FileVisitResult.SKIP_SUBTREE;
        final Path relative = base.relativize(dir);
        System.out.println("Entering directory " + relative);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final Path relative = base.relativize(file);
        System.out.println("Found file " + relative);
        final Element fileElement = doc.createElement("FILE");
        fileElement.setAttribute("path", relative.toString());
        rootElement.appendChild(fileElement);
        if(doMd5) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException ex) {
                System.err.println("CRITICAL: This JVM has no MD5 checksum implementation!");
                ex.printStackTrace();
                System.exit(-1);
            }
            try (InputStream is = Files.newInputStream(Paths.get(file.toString()));
                    DigestInputStream dis = new DigestInputStream(is, md)) {
                //https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
                /* Read decorated stream (dis) to EOF as normal... */
                final int bufferSize = 1024*1024*1024;
                while(dis.read(new byte[bufferSize]) != -1) {
                    //keep reading bytes into the digest
                }
                byte[] md5Bytes = md.digest();
                String md5String = javax.xml.bind.DatatypeConverter.printHexBinary(md5Bytes);
                Element md5Element = doc.createElement("MD5");
                md5Element.setTextContent(md5String);
                fileElement.appendChild(md5Element);
            }
        }
        if(useFFprobe && file.toString().endsWith(".wtv")) {
            ProcessBuilder pb = new ProcessBuilder("ffprobe", file.toString());
            Process p = pb.start();
            try {
                p.waitFor();
            }
            catch(InterruptedException ex) {
                System.err.println("Interrupted while waiting for process. How could this happen?");
                ex.printStackTrace();
            }
            if(p.exitValue() != 0) {
                System.err.println("Error calling ffprobe for file " + file);
                System.err.println(p.exitValue());
            }
            else {
                BufferedReader ffprobe = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line = ffprobe.readLine())!=null) {
                    String[] parts = line.split(":", 2);
                    String possibleKey = parts[0].trim();
                    WtvMetadata corresponding = null;
                    for(WtvMetadata wtvMeta : WtvMetadata.values()) {
                        if(possibleKey.equals(wtvMeta.getWtvMetadataKey())) {
                            corresponding = wtvMeta;
                            break;
                        }
                    }
                    if(corresponding != null) {
                        String value = parts[1].trim();
                        String append = "";
                        if(corresponding == WtvMetadata.DURATION) {
                            //ffprobe/WTV has two duration values: one as
                            //100*nanoseconds and another as hrs:mins:secs.fraction
                            String durationParts[] = value.split(",");
                            if(durationParts.length > 1) { //is hrs:mins:secs
                                append = "_READABLE";
                                value = durationParts[0];
                            }
                            else {
                                append = "_100NANOS";
                            }
                        }
                        Element wtvData = doc.createElement(corresponding.name() + append);
                        wtvData.setTextContent(value);
                        fileElement.appendChild(wtvData);
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("Failed to visit " + base.relativize(file));
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("Exiting directory " + base.relativize(dir));
        return FileVisitResult.CONTINUE;
    }
}
