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
package aprel.tags.db;

import aprel.tags.xml.Xml;
import aprel.tags.xml.XmlTag;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Aprel
 */
public interface TableTag {
    static Set<TableTag> xmlSet = Collections.unmodifiableSet(Stream.concat(Stream.concat(
            Arrays.stream(Files.values()),
            Arrays.stream(Directories.values())),
            Arrays.stream(Metadata.values()))
            .filter(e -> e.getCorrespondingXmlTag() != Xml.NO_XML)
            .collect(Collectors.toSet()));
    
    public String getTableName();
    public String getColumnHeader();
    /**
     * 
     * @return the <code>XmlTag</code> that corresponds to the XML representation 
     * of this datum, or <code>Xml.NO_XML</code> if this datum is not stored in 
     * the METADATA XML file.
     */
    public XmlTag getCorrespondingXmlTag();
    
    /**
     * 
     * @return the set of <code>TableTag</code>s whose <code>getCorrespondingXmlTag()
     * </code> method does not return <code>Xml.NO_XML</code>.
     */
    public static Set<TableTag> getSetWithXmlCorrespondence() {
        return xmlSet;
    }
}
