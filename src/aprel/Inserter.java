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
import aprel.jdbi.Insert;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
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
public class Inserter {
    
    /**
     * The local path to the directory of files to be archived.
     */
    private static final String OPTION_INPUT_PATH = "i";
    /**
     * The destination path in the catalog.
     */
    private static final String OPTION_OUTPUT_PATH = "o";
    /**
     * The catalog in the archive to receive the files.
     */
    private static final String OPTION_CATALOG = "c";
    private static final Logger LOG = LoggerFactory.getLogger(Inserter.class);
    
    private static String archivePath, localPath, catalog;
    
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_INPUT_PATH).required()
                .desc("the local path to the directory of files to be archived").numberOfArgs(1).build());
        options.addOption(Option.builder(OPTION_OUTPUT_PATH).required()
                .desc("the destination path in the catalog").numberOfArgs(1).build());
        options.addOption(Option.builder(OPTION_CATALOG).required().longOpt("catalog")
                .desc("the catalog in the archive to receive the files").numberOfArgs(1).build());
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(Inserter.class.getSimpleName(), options);

            System.exit(1);
            return;
        }
        archivePath = cmd.getOptionValue(OPTION_OUTPUT_PATH);
        localPath = cmd.getOptionValue(OPTION_INPUT_PATH);
        catalog = cmd.getOptionValue(OPTION_CATALOG);
        String metadataFileLoc = localPath + (localPath.endsWith("/") ? "" : "/") + "METADATA.xml";
        File metadataFile = new File(metadataFileLoc);
        if(!metadataFile.exists()) {
            LOG.error("No METADATA.xml at " + metadataFile.getAbsolutePath());
            return;
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(FilesRootContainer.class);
        Unmarshaller unmarsh = jaxbContext.createUnmarshaller();
        FilesRootContainer files = (FilesRootContainer) unmarsh.unmarshal(metadataFile);
        List<FileBean> l = files.getFiles();
        //files are not on optical; they are on local storage
        l.stream().forEach(bean ->  {
            bean.setOnOptical(false);
            bean.setOnLocalDisc(true);
                });
        
        ArchiveDatabase db = ArchiveDatabase.createDefaultDatabase();
        Insert ins = db.getHandle().attach(Insert.class);
        ins.insertAllNoMetadata(l.iterator());
        
        db.close();
    }
}
