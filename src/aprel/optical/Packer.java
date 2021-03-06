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

import aprel.db.beans.FileBean;
import java.util.Collection;
import java.util.List;

/**
 *
 * A Packer takes a collection of files, chunks them into Parts, and packs the 
 * parts into Opticals. Each implementation can choose how best to accomplish 
 * this. In addition, implementations may choose not to pack certain files. 
 * However, Packers must take care that if they choose to pack a file, they must 
 * pack it completely, with no parts excluded.
 * 
 * Packers must set the parent, size, and offset of the Parts they create, but 
 * they do not have to set the "ordinal" and "totalInSet" properties, which must 
 * be set externally.
 * 
 * @author Aprel
 */
public interface Packer {
    /**
     * 
     * @param files
     * @param maxOpticals the "maximum" size of the returned list, but in fact, 
     * the list can have fewer or more than this value. Since packing is hard, 
     * it may not be feasible to maintain this maximum with efficient packing, so 
     * implementations should try their best to approximate this value, but it is 
     * not a hard requirement.
     * @param startingSet Opticals to include at the beginning of the returned 
     * list. If the last Optical in the list is not full, adds more file parts 
     * to that Optical. Can be an empty list, but not {@code null}. If 
     * {{@code startingSet.size()} {@literal > maxOpticals}}, throws
     * {@code IllegalArgumentExpection}.
     * 
     * @return 
     */
    public List<Optical> packFilesIntoOpticals(Collection<FileBean> files, 
            int maxOpticals, List<Optical> startingSet);
}
