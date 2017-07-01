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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aprel
 */
public class OpticalSet {
    private final List<Optical> opticals;
    private Optical currentDisc;
    
    public OpticalSet() {
        opticals = new ArrayList<>();
        currentDisc = createNewOptical();
    }
    
    private Optical createNewOptical() {
        Optical o = new Optical();
        opticals.add(o);
        return o;
    }
    
    public List<Optical> getOpticals() {
        return new ArrayList<>(opticals);
    }
    
    public void add(FileBean file) {
        final long fileSize = file.getSize();
        long bytesOfFileRemaining = fileSize;
        do {
            Part p = new Part(file);
            final long bytesToWrite = Math.min(bytesOfFileRemaining, currentDisc.getAvailableSpace());
            p.setSize(bytesToWrite);
            p.setOffset(fileSize - bytesOfFileRemaining);
            bytesOfFileRemaining -= bytesToWrite;
            currentDisc.add(p);
            if(currentDisc.getAvailableSpace() == 0)
                currentDisc = createNewOptical();
        } while(bytesOfFileRemaining > 0);
        if(bytesOfFileRemaining != 0)
            throw new IllegalStateException("Illegal division of file into parts: " 
                    + bytesOfFileRemaining);
    }
}
