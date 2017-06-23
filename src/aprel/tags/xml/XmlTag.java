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
package aprel.tags.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Aprel
 */
public interface XmlTag {
    static Set<XmlTag> fileTagSet = Collections.unmodifiableSet(Stream.concat(
            Arrays.stream(WtvMetadata.values()), 
            Arrays.stream(FileMetadata.values()))
            .collect(Collectors.toSet()));
    
    public String getXmlTag();
    
    /**
     * 
     * @return The set of all possible children tags for a file element.
     */
    public static Set<XmlTag> getFileTagSet() {
        return fileTagSet;
    }
}
