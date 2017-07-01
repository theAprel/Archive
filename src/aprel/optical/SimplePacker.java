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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;

/**
 *
 * Packs files into opticals, preferring to pack files with the same parent 
 * directory into the same optical.
 * 
 * Does not pack files if they cannot fill an Optical to max capacity.
 * @author Aprel
 */
public class SimplePacker implements Packer {

    @Override
    public List<Optical> packFilesIntoOpticals(Collection<FileBean> files) {
        Multimap<String,FileBean> parentIdToFiles = HashMultimap.create();
        files.forEach(f -> {
            parentIdToFiles.put(f.getDirParentId(), f);
        });
        OpticalSet opticals = new OpticalSet();
        parentIdToFiles.asMap().forEach((dir, fs) -> {
            fs.forEach(f -> opticals.add(f));
        });
        List<Optical> oList = opticals.getOpticals();
        //remove the last Optical if it's not full
        final Optical lastOptical = oList.get(oList.size()-1);
        if(lastOptical.getAvailableSpace() != 0)
            oList.remove(oList.size()-1);
        return oList;
    }
    
}
