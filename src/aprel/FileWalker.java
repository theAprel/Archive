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
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author Aprel
 */
public class FileWalker implements FileVisitor<Path> {
    
    private final Path base;
    private final DirectoryNode root;
    private DirectoryNode currentDir;
    private final LinkedList<DirectoryNode> walkPath;
    private static final boolean useFFprobe = true;
    
    public static void main(String[] args) throws Exception {
        Path p = Paths.get(System.getProperty("user.home") + "/Videos");
        FileWalker fw = new FileWalker(p);
        Files.walkFileTree(p, fw);
        Document doc = fw.toXml();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("/tmp/output.xml"));
        transformer.transform(source, result);
    }
    
    public FileWalker(Path base) {
        this.base = base;
        root = new DirectoryNode("root");
        currentDir = root;
        walkPath = new LinkedList<>();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        final Path relative = base.relativize(dir);
        System.out.println("Entering directory " + relative);
        walkPath.push(currentDir);
        final DirectoryNode newDir = new DirectoryNode(dir.getFileName().toString());
        currentDir.addFile(newDir);
        currentDir = newDir;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final Path relative = base.relativize(file);
        System.out.println("Found file " + relative);
        final FileNode fileNode = new FileNode(file.getFileName().toString());
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
                    System.out.println(line);
                }
            }
        }
        
        currentDir.addFile(fileNode);
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
        currentDir = walkPath.pop();
        return FileVisitResult.CONTINUE;
    }
    
    public Document toXml() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        root.toXml(doc, doc);
        return doc;
    }

}
