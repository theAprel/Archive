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
package aprel.optical;

import aprel.ArchiveDatabase;
import aprel.db.beans.FileBean;
import aprel.jdbi.Insert;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

/**
 *
 * @author Aprel
 */
public class Isoifier {
    
    public static final String FILENAME_ORDINAL_SEPARATOR = "-";
    private static final String TEMPORARY_DIR_PREFIX = "java-archive-udf";
    
    private static final String OPTION_TEMP_DIRECTORY = "t";
    private static final String OPTION_ISO_OUTPUT = "o";
    private static final String OPTION_CREATE_FIRST_DISC_IN_CATALOG = "create-first-disc";
    private static final String OPTION_MAX_OPTICAL = "max";
    private static final String OPTION_LEFTOVER_XML_FILE = "l";
    private static final String OPTION_NO_LEFTOVERS_OUTSTANDING = "no-leftovers-outstanding";
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_TEMP_DIRECTORY).longOpt("temp")
                .desc("directory where temporary files will be stored prior to "
                        + "their inclusion in the UDF image. If not specified, "
                        + "the default temporary directory provided by the "
                        + "filesystem will be used.").numberOfArgs(1).build());
        options.addOption(Option.builder().longOpt(OPTION_CREATE_FIRST_DISC_IN_CATALOG)
                .desc("set this option if there are no discs in this catalog yet.")
                .numberOfArgs(0).build());
        options.addOption(Option.builder(OPTION_ISO_OUTPUT).required()
                .desc("directory for UDF image files").numberOfArgs(1).build());
        options.addOption(Option.builder(OPTION_MAX_OPTICAL)
                .desc("maximum number of image files to create. If not set, this"
                        + " will keep creating UDF images until all files not yet"
                        + " on optical in the database have been copied.")
                .numberOfArgs(1).build());
        options.addOption(Option.builder(OPTION_LEFTOVER_XML_FILE).longOpt("leftovers")
                .desc("XML for file chunks that should be included when more"
                        + " data is available to fill the optical medium.")
                .required().numberOfArgs(1).build());
        options.addOption(Option.builder().longOpt(OPTION_NO_LEFTOVERS_OUTSTANDING)
                .desc("set this option if there are no leftover file chunks to be"
                        + " written to optical. This can only occur when a catalog"
                        + " has no discs yet, or with extremely intelligent "
                        + "packing algorithms.").numberOfArgs(0).build());
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(Isoifier.class.getSimpleName(), options);

            System.exit(1);
            return;
        }
        final int maxOptical = cmd.hasOption(OPTION_MAX_OPTICAL) ? 
                Integer.parseInt(cmd.getOptionValue(OPTION_MAX_OPTICAL)) 
                : Integer.MAX_VALUE;
        final boolean createFirstDiscInCatalog = cmd.hasOption(
                OPTION_CREATE_FIRST_DISC_IN_CATALOG);
        
        String udfDir = cmd.getOptionValue(OPTION_ISO_OUTPUT);
        if(!new File(udfDir).isDirectory()) {
            System.err.println(udfDir + " is not a directory");
            System.exit(1);
            return;
        }
        udfDir += udfDir.endsWith(File.separator) ? "" : File.separator;
        
        String leftoverXmlFile = cmd.getOptionValue(OPTION_LEFTOVER_XML_FILE);
        if(new File(leftoverXmlFile).isDirectory()) {
            System.err.println(leftoverXmlFile + " is a directory.");
            System.exit(1);
            return;
        }
        if(!new File(leftoverXmlFile).exists()) {
            if(!cmd.hasOption(OPTION_NO_LEFTOVERS_OUTSTANDING)) {
                System.err.println(leftoverXmlFile + " does not exist. Use --" + 
                        OPTION_NO_LEFTOVERS_OUTSTANDING + " if there really are "
                                + "no leftover file chunks waiting to be written to optical.");
                System.exit(1);
                return;
            }
        }
        if(cmd.hasOption(OPTION_NO_LEFTOVERS_OUTSTANDING) && new File(leftoverXmlFile).exists()) {
            System.err.println("There are leftover file chunks outstanding. "
                    + "Do not use --" + OPTION_NO_LEFTOVERS_OUTSTANDING);
            System.exit(1);
            return;
        }
        
        final JAXBContext jaxbContext = JAXBContext.newInstance(PartsRootContainer.class);
        
        Path temporaryDirectory;
        if(cmd.hasOption(OPTION_TEMP_DIRECTORY)) {
            final Path pathGiven =  Paths.get(cmd.getOptionValue(OPTION_TEMP_DIRECTORY));
            if(!Files.exists(pathGiven)) {
                System.err.println("Temp directory does not exist.");
                System.exit(1);
                return;
            }
            else if(!Files.isDirectory(pathGiven)) {
                System.err.println("Temporary location is not a directory.");
                System.exit(1);
                return;
            }
            temporaryDirectory = Paths.get(pathGiven.toAbsolutePath().toString() 
                    + File.separator + TEMPORARY_DIR_PREFIX);
        }
        else {
            temporaryDirectory = Files.createTempDirectory(TEMPORARY_DIR_PREFIX);
        }
        temporaryDirectory.toFile().deleteOnExit();
        final String temporaryBasePath = temporaryDirectory + File.separator;
        
        ArchiveDatabase db = ArchiveDatabase.createDefaultDatabase();
        final List<FileBean> notOnOptical = db.getQueryObject().getAllFilesNotOnOptical();
        Packer packer = new SimplePacker();
        List<Optical> opticals = packer.packFilesIntoOpticals(notOnOptical, maxOptical);
        //set properties ordinal and totalInSet
        final Map<FileBean,List<Part>> fileToParts = new HashMap<>();
        opticals.forEach(op -> {
            op.getParts().forEach(p -> {
                FileBean file = p.getParent();
                if(!fileToParts.containsKey(file))
                    fileToParts.put(file, new ArrayList<>());
                //set catalog here
                p.setCatalog(file.getCatalog());
                fileToParts.get(file).add(p);
            });
        });
        //since all parts are placed in the same directory prior to writing,
        //we need to give them a temp unique id to prevent overwriting files
        //that used to be in different directories but have the same name
        //this temp unique id will be stripped when the parts acquire their db ids
        long uniqueId = 0;
        for(Map.Entry<FileBean,List<Part>> entry : fileToParts.entrySet()) {
            FileBean file = entry.getKey();
            List<Part> parts = entry.getValue();
            final int length = parts.size();
            if(length == 1) {
                // this is where all singleton parts (unsplit files) have their props set 
                final Part singleton = parts.get(0);
                singleton.setMd5(file.getMd5());
                singleton.setPartFilename(uniqueId++ + FILENAME_ORDINAL_SEPARATOR + file.getFilename());
                singleton.setOrdinal(1);
                singleton.setTotalInSet(1);
            }
            else {
                for(int i = 1; i <= length; i++) {
                    Part p = parts.get(i-1);
                    p.setOrdinal(i);
                    p.setTotalInSet(length);
                    p.setPartFilename(uniqueId++ + FILENAME_ORDINAL_SEPARATOR + file.getFilename());
                }
            }
        }
        
        final String partsDir = temporaryDirectory.toString();
        final Insert ins = db.getInsertObject();
        final String catalog = opticals.stream().findAny().get().getParts()
                .stream().findAny().get().getCatalog();
        int discNumber = db.getQueryObject().getLastDiscNumberInSeries(catalog);
        if(createFirstDiscInCatalog) {
            if(discNumber == 0) {
                discNumber = 1;
            }
            else {
                System.err.println("Received --" + OPTION_CREATE_FIRST_DISC_IN_CATALOG
                        + " but there are already discs in this catalog. Exit.");
                System.exit(1);
                return;
            }
        }
        else if(discNumber == 0) {
            System.err.println("Catalog has no discs. Use --" + 
                    OPTION_CREATE_FIRST_DISC_IN_CATALOG + " to create the first disc.");
            System.exit(1);
            return;
        }
        final Optical leftover = opticals.get(opticals.size()-1)
                .getAvailableSpace() != 0 ? opticals.remove(opticals.size()-1) : null;
        for(Optical opt : opticals) {
            opt.writePartsToDir(partsDir);
            //at this point, parts should have all their database fields set
            //commit to database
            final List<Part> parts = opt.getParts();
            for(Part p : parts) {
                if(!p.getCatalog().equals(catalog)) {
                    System.err.println("Multiple catalogs detected. Exit.");
                    System.exit(1);
                    return;
                }
                p.setOnOptical(true);
                p.setMd5Verified(false);
                p.setDiscNumber(discNumber);
                final String id = ins.insertPart(p);
                p.setId(id);
                //now that parts have db ids, rename them to conform to "id-filename-ordinal"
                com.google.common.io.Files.move(
                        new File(temporaryBasePath + p.getPartFilename()),
                        new File(temporaryBasePath + p.getId() + FILENAME_ORDINAL_SEPARATOR 
       /*strip temp uniqid-->*/ + p.getPartFilename().split(FILENAME_ORDINAL_SEPARATOR,2)[1] 
                                + FILENAME_ORDINAL_SEPARATOR 
                                + p.getOrdinal() + FILENAME_ORDINAL_SEPARATOR 
                                + p.getTotalInSet()));
            }
            //now, package the contents of the temp directory to a udf image, then clear the temp files
            final String udfFilename = catalog + FILENAME_ORDINAL_SEPARATOR + discNumber;
            ProcessBuilder pb = new ProcessBuilder("mkisofs", "-R", "-J", "-udf", 
                    "-iso-level", "3", "-V", udfFilename, "-o", udfDir + udfFilename + ".iso", temporaryBasePath)
                    .inheritIO();
            System.out.println("Starting mkisofs...");
            Process p = pb.start();
            p.waitFor();
            System.out.println("Successfully saved to " + udfFilename + ".iso");
            Arrays.asList(temporaryDirectory.toFile().listFiles()).forEach(f -> f.delete());
            
            discNumber++;
        }
        //now, handle the leftovers
        if(leftover == null) {
            //we wrote the leftovers and don't have any to save, so delete the xml
            new File(leftoverXmlFile).delete();
        }
        else {
            Marshaller marsh = jaxbContext.createMarshaller();
            JAXBSource jsource = new JAXBSource(marsh, new PartsRootContainer(leftover.getParts()));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StreamResult result = new StreamResult(leftoverXmlFile);
            transformer.transform(jsource, result);
        }
        
        db.close();
    }
}
