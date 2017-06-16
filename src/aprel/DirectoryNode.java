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

import java.nio.file.FileAlreadyExistsException;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Aprel
 */
public class DirectoryNode implements FileSystemObjectNode {
    private final String name;
    private final Set<FileSystemObjectNode> contents;

    public DirectoryNode(String name) {
        this.name = name;
        this.contents = new HashSet<>();
    }
    
    public void addFile(FileSystemObjectNode f) throws FileAlreadyExistsException {
        for(FileSystemObjectNode fInSet : contents) {
            if(fInSet.getName().equalsIgnoreCase(f.getName()))
                throw new FileAlreadyExistsException(fInSet.getName());
        }
        contents.add(f);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void toXml(Document doc, Node parent) {
        Element thisDirectory = doc.createElement("directory");
        thisDirectory.setAttribute("name", name);
        parent.appendChild(thisDirectory);
        contents.forEach((fso) -> {
            fso.toXml(doc, thisDirectory);
        });
    }
    
    
}
