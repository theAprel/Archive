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

/**
 *
 * @author Aprel
 */
public class Part {
    /*
    (Serial) id | partFilename | parentFileId (serial) | ordinal | totalInSet |
    md5 | size | BOOL onOptical | BOOL md5Verified | catalog | discNumber | 
    localStoragePath (tmp until written to optical)
    */
    private String id, partFilename, parentFileId, md5, catalog, localStoragePath;
    private boolean onOptical, md5Verified;
    private int ordinal, totalInSet, discNumber;
    private final FileBean parent;
    private long offset = 0L;
    private long size = 0L;
    
    public Part(FileBean parent) {
        this.parent = parent;
        parentFileId = parent.getId();
        catalog = parent.getCatalog();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public FileBean getParent() {
        return parent;
    }

    public String getParentFileId() {
        return parentFileId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getTotalInSet() {
        return totalInSet;
    }

    public void setTotalInSet(int totalInSet) {
        this.totalInSet = totalInSet;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPartFilename() {
        return partFilename;
    }

    public void setPartFilename(String partFilename) {
        this.partFilename = partFilename;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public boolean isOnOptical() {
        return onOptical;
    }

    public void setOnOptical(boolean onOptical) {
        this.onOptical = onOptical;
    }

    public boolean isMd5Verified() {
        return md5Verified;
    }

    public void setMd5Verified(boolean md5Verified) {
        this.md5Verified = md5Verified;
    }

    public int getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(int discNumber) {
        this.discNumber = discNumber;
    }

    @Override
    public String toString() {
        return "Part{" + "id=" + id + ", partFilename=" + partFilename + 
                ", parentFileId=" + parentFileId + ", md5=" + md5 + ", catalog=" 
                + catalog + ", localStoragePath=" + localStoragePath + 
                ", onOptical=" + onOptical + ", md5Verified=" + md5Verified + 
                ", ordinal=" + ordinal + ", totalInSet=" + totalInSet + 
                ", discNumber=" + discNumber + ", parent=" + parent + ", offset=" 
                + offset + ", size=" + size + '}';
    }
    
}
