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

import java.util.Collection;

/**
 *
 * A PrivilegedOptical hides a Part until {@code becomeNormal} is called. This 
 * class is intended to hold a leftover Part that has already gone through 
 * database pre-processing and to prevent the Part from having its properties 
 * re-set.
 * @author Aprel
 */
public class PrivilegedOptical extends Optical {
    
    private final Collection<Part> hiddenParts;
    private boolean isNormal = false;
    
    public PrivilegedOptical(Collection<Part> toHide) {
        super();
        final long colSize = toHide.stream().mapToLong(Part::getSize).sum();
        if(colSize > spaceRemaining)
            throw new IllegalArgumentException("Part collection size exceeds "
                    + "available optical space: " + colSize + " > " + spaceRemaining);
        hiddenParts = toHide;
        spaceRemaining -= colSize;
    }
    
    public void becomeNormal() {
        if(isNormal)
            throw new IllegalStateException("Already normal");
        parts.addAll(0, hiddenParts);
        
        isNormal = true;
    }
    
}
