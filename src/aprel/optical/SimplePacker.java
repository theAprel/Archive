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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<Optical> packFilesIntoOpticals(final Collection<FileBean> files, 
            final int maxOpticals, List<Optical> startingSet) {
        if(startingSet.size() >= maxOpticals)
            throw new IllegalArgumentException("startingSet.size() > maxOpticals: "
                    + startingSet.size() + " > " + maxOpticals);
        Multimap<String,FileBean> parentIdToFiles = HashMultimap.create();
        files.forEach(f -> {
            parentIdToFiles.put(f.getDirParentId(), f);
        });
        OpticalSet opticals = startingSet.isEmpty() ? new OpticalSet() 
                : new OpticalSet(startingSet);
        iter:
        for(Map.Entry<String,Collection<FileBean>> entry : parentIdToFiles.asMap()
                .entrySet()) {
            final Collection<FileBean> fs = entry.getValue();
            //sort by filesize, smallest first
            final List<FileBean> sortBySizeSmallFirst = new ArrayList<>(fs);
            sortBySizeSmallFirst.sort((f1, f2) -> {
                long diff = f1.getSize() - f2.getSize();
                if(diff > Integer.MAX_VALUE)
                    return Integer.MAX_VALUE;
                if(diff < Integer.MIN_VALUE)
                    return Integer.MIN_VALUE;
                return (int) diff;
            });
            for(FileBean f : fs) {
                opticals.add(f);
                if(opticals.size() > maxOpticals)
                    break iter;
            }
        }
        final List<Optical> oList = opticals.getOpticals();
        //verify that no files are incomplete
        if(hasIncompleteFiles(oList))
            throw new IllegalStateException("Fix this implementation. Parts are"
                    + " missing in the Opticals.");
        return oList;
    }
    
    private static boolean hasIncompleteFiles(final List<Optical> oList) {
        final Map<Part,Optical> partToOptical = new HashMap<>();
        oList.forEach(opt -> {
            opt.getParts().forEach((p) -> {
                partToOptical.put(p, opt);
            });
        });
        final Collection<Part> parts = partToOptical.keySet();
        final Multimap<FileBean,Part> parentToParts = HashMultimap.create();
        parts.forEach(p -> parentToParts.put(p.getParent(), p));
        for(Map.Entry<FileBean,Collection<Part>> entry : parentToParts.asMap().entrySet()) {
            final FileBean file = entry.getKey();
            final Collection<Part> childParts = entry.getValue();
            if(file.getSize() != childParts.stream().mapToLong(Part::getSize).sum()) {
                //found an incomplete file
                return true;
}
        }
        return false;
    }
    
}
