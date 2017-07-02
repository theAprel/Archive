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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            final int maxOpticals) {
        Multimap<String,FileBean> parentIdToFiles = HashMultimap.create();
        files.forEach(f -> {
            parentIdToFiles.put(f.getDirParentId(), f);
        });
        OpticalSet opticals = new OpticalSet();
        iter:
        for(Map.Entry<String,Collection<FileBean>> entry : parentIdToFiles.asMap()
                .entrySet()) {
            final Collection<FileBean> fs = entry.getValue();
            for(FileBean f : fs) {
                opticals.add(f);
                if(opticals.size() > maxOpticals)
                    break iter;
            }
        }
        final List<Optical> oList = opticals.getOpticals();
        //remove the last Optical if it's not full
        final Optical lastOptical = oList.get(oList.size()-1);
        if(lastOptical.getAvailableSpace() != 0)
            oList.remove(oList.size()-1);
        //now, go through all the Parts and throw away Opticals with incomplete partial files
        
        while(hasIncompleteFilesAndPrune(oList)) { /* keep pruning */ }
        return oList;
    }
    
    private static boolean hasIncompleteFilesAndPrune(final List<Optical> oList) {
        final Map<Part,Optical> partToOptical = new HashMap<>();
        oList.forEach(opt -> {
            opt.getParts().forEach((p) -> {
                partToOptical.put(p, opt);
            });
        });
        final Collection<Part> parts = partToOptical.keySet();
        boolean hasIncompletes = false;
        final Multimap<FileBean,Part> parentToParts = HashMultimap.create();
        parts.forEach(p -> parentToParts.put(p.getParent(), p));
        for(Map.Entry<FileBean,Collection<Part>> entry : parentToParts.asMap().entrySet()) {
            final FileBean file = entry.getKey();
            final Collection<Part> childParts = entry.getValue();
            if(file.getSize() != childParts.stream().mapToLong(Part::getSize).sum()) {
                //found an incomplete file
                final Set<Optical> toRemove = new HashSet<>();
                childParts.forEach(p -> {
                    toRemove.add(partToOptical.get(p));
                });
                oList.removeAll(toRemove);
                hasIncompletes = true;
            }
        }
        return hasIncompletes;
    }
    
}
