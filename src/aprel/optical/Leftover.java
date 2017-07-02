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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Aprel
 */
@XmlRootElement( name = "leftover")
public class Leftover {
    private Part part;
    private String parentFilename, parentLocalStoragePath;
    
    public Leftover() {
        
    }
    
    public Leftover(Part p) {
        part = p;
        parentFilename = p.getParent().getFilename();
        parentLocalStoragePath = p.getParent().getLocalStoragePath();
    }

    public Part getPart() {
        return part;
    }

    @XmlElement(name = "part", type = Part.class)
    public void setPart(Part part) {
        this.part = part;
    }

    public String getParentFilename() {
        return parentFilename;
    }

    public void setParentFilename(String parentFilename) {
        this.parentFilename = parentFilename;
    }

    public String getParentLocalStoragePath() {
        return parentLocalStoragePath;
    }

    public void setParentLocalStoragePath(String parentLocalStoragePath) {
        this.parentLocalStoragePath = parentLocalStoragePath;
    }
    
    public static List<Leftover> createLeftovers(Collection<Part> parts) {
        final List<Leftover> list = new ArrayList<>();
        parts.forEach(p -> list.add(new Leftover(p)));
        return list;
    }
}
