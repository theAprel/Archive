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

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Aprel
 */
public class FileNode implements FileSystemObjectNode {
    private final String name;
    private final Map<String, String> metadata;

    public FileNode(String name) {
        this.name = name;
        this.metadata = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void toXml(Document doc, Node parent) {
        Element thisFile = doc.createElement("file");
        thisFile.setAttribute("name", name);
        parent.appendChild(thisFile);
    }
}
