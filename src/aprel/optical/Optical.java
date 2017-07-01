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

/**
 *
 * Not thread-safe.
 * @author Aprel
 */
public class Optical {
    private final List<Part> parts;
    private long spaceRemaining;
    
    public static final long MAX_BYTES = 25L * 1000L * 1000L * 1000L; //Bluray
    public static final int OVERHEAD = 5 * 1000 * 1000; // 5MB to be on the safe side
    public static final long PAYLOAD_SPACE = MAX_BYTES - OVERHEAD;
    
    public Optical() {
        parts = new ArrayList<>();
        spaceRemaining = PAYLOAD_SPACE;
    }
    
    public void add(Part p) {
        final long size = p.getSize();
        if(size > spaceRemaining)
            throw new IllegalArgumentException("Part exceeds available space: " 
                    + size + " > " + spaceRemaining);
        parts.add(p);
        spaceRemaining -= size;
    }
    
    public long getAvailableSpace() {
        return spaceRemaining;
    }

    @Override
    public String toString() {
        return "Optical{" + "parts=" + parts + ", spaceRemaining=" + spaceRemaining + '}';
    }
}
