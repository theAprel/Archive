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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Aprel
 */
@XmlRootElement(name = "part")
public class Part {
    /*
    (Serial) id | partFilename | parentFileId (serial) | ordinal | totalInSet |
    md5 | size | BOOL onOptical | BOOL md5Verified | catalog | discNumber | 
    localStoragePath (tmp until written to optical)
    */
    private String id, partFilename, parentFileId, md5, catalog, localStoragePath;
    private boolean onOptical, md5Verified, leftover;
    private int ordinal, totalInSet, discNumber;
    private FileBean parent;
    private long offset = 0L;
    private long size = 0L;
    
    public Part() {
        
    }
    
    public Part(FileBean parent) {
        this.parent = parent;
        parentFileId = parent.getId();
        catalog = parent.getCatalog();
    }

    public String getId() {
        return id;
    }

    @XmlTransient
    public void setId(String id) {
        checkLeftover();
        this.id = id;
    }
    
    public FileBean getParent() {
        return parent;
    }

    @XmlElement
    public void setParentFileId(String parentFileId) {
        this.parentFileId = parentFileId;
    }

    @XmlElement
    public void setParent(FileBean parent) {
        this.parent = parent;
    }

    public String getParentFileId() {
        return parentFileId;
    }

    public long getOffset() {
        return offset;
    }

    @XmlElement
    public void setOffset(long offset) {
        checkLeftover();
        this.offset = offset;
    }

    public long getSize() {
        return size;
    }

    @XmlElement
    public void setSize(long size) {
        checkLeftover();
        this.size = size;
    }

    public int getOrdinal() {
        return ordinal;
    }

    @XmlElement
    public void setOrdinal(int ordinal) {
        checkLeftover();
        this.ordinal = ordinal;
    }

    public int getTotalInSet() {
        return totalInSet;
    }

    @XmlElement
    public void setTotalInSet(int totalInSet) {
        checkLeftover();
        this.totalInSet = totalInSet;
    }

    public String getMd5() {
        return md5;
    }

    @XmlTransient
    public void setMd5(String md5) {
        checkLeftover();
        this.md5 = md5;
    }

    public String getPartFilename() {
        return partFilename;
    }

    @XmlElement
    public void setPartFilename(String partFilename) {
        checkLeftover();
        this.partFilename = partFilename;
    }

    public String getCatalog() {
        return catalog;
    }

    @XmlElement
    public void setCatalog(String catalog) {
        checkLeftover();
        this.catalog = catalog;
    }

    public boolean isOnOptical() {
        return onOptical;
    }

    @XmlTransient
    public void setOnOptical(boolean onOptical) {
        this.onOptical = onOptical;
    }

    public boolean isMd5Verified() {
        return md5Verified;
    }

    @XmlTransient
    public void setMd5Verified(boolean md5Verified) {
        this.md5Verified = md5Verified;
    }

    public int getDiscNumber() {
        return discNumber;
    }

    @XmlTransient
    public void setDiscNumber(int discNumber) {
        this.discNumber = discNumber;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    public void setLocalStoragePath(String localStoragePath) {
        this.localStoragePath = localStoragePath;
    }
    
    public boolean isLeftover() {
        return leftover;
    }

    @XmlTransient
    public void setLeftover(boolean leftover) {
        this.leftover = leftover;
    }
    
    private void checkLeftover() {
        if(leftover)
            throw new IllegalStateException("This Part is a leftover; "
                    + "you should not alter this attribute.");
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
