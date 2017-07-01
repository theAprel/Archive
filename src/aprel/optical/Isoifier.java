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

import aprel.ArchiveDatabase;
import aprel.db.beans.FileBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Aprel
 */
public class Isoifier {
    
    public static final String FILENAME_ORDINAL_SEPARATOR = "-";
    
    public static void main(String[] args) throws Exception {
        ArchiveDatabase db = ArchiveDatabase.createDefaultDatabase();
        final List<FileBean> notOnOptical = db.getQueryObject().getAllFilesNotOnOptical();
        Packer packer = new SimplePacker();
        List<Optical> opticals = packer.packFilesIntoOpticals(notOnOptical);
        //set properties ordinal and totalInSet
        final Map<FileBean,List<Part>> fileToParts = new HashMap<>();
        opticals.forEach(op -> {
            op.getParts().forEach(p -> {
                FileBean file = p.getParent();
                if(!fileToParts.containsKey(file))
                    fileToParts.put(file, new ArrayList<>());
                //set catalog here
                p.setCatalog(file.getCatalog());
                fileToParts.get(file).add(p);
            });
        });
        fileToParts.forEach((file, parts) -> {
            final int length = parts.size();
            if(length == 1) {
                // this is where all singleton parts (unsplit files) have their props set 
                final Part singleton = parts.get(0);
                singleton.setMd5(file.getMd5());
                singleton.setPartFilename(file.getFilename());
                singleton.setOrdinal(1);
                singleton.setTotalInSet(1);
            }
            else {
                for(int i = 1; i <= length; i++) {
                    Part p = parts.get(i-1);
                    p.setOrdinal(i);
                    p.setTotalInSet(length);
                    p.setPartFilename(file.getFilename() + FILENAME_ORDINAL_SEPARATOR + i);
                }
            }
        });
        
        List<Part> multipart = fileToParts.values().stream().flatMap(List::stream)
                .filter(p -> p.getTotalInSet() > 1).collect(Collectors.toList());
        System.out.println(multipart);
        
        db.close();
    }
}
