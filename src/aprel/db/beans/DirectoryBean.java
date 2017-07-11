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
package aprel.db.beans;

import aprel.ArchiveDatabase;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Aprel
 */
public class DirectoryBean implements DbFile {
    private String id, dirName, dirParentId;
    private DirectoryBean parent;
    private boolean idHasBeenSet = false;

    public boolean existsInDatabase() {
        return idHasBeenSet;
    }
    
    /**
     * Trusts that external code has verified that this directory is empty. If 
     * this directory is not empty, calling this method will corrupt the 
     * database.
     */
    void delete(ArchiveDatabase db) {
        db.getDeleteObject().deleteDir(this);
    }
    
    /**
     * 
     * @return the id in database, or {@code null} if not in database.
     */
    @Override
    public String getId() {
        //it would be nice to have this check, but we can't b/c of JavaBeans
//        if(!idHasBeenSet)
//            throw new IllegalStateException("getId that has not been set");
        return id;
    }

    public void setId(String id) {
        this.id = id;
        idHasBeenSet = true;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }
    
    @XmlTransient
    @Override
    public String getName() {
        return getDirName();
    }

    public String getDirParentId() {
        if(parent != null)
            return parent.getId();
        return dirParentId;
    }

    public void setDirParentId(String dirParentId) {
        this.dirParentId = dirParentId;
        parent = null; //remove reference to parent
    }

    public DirectoryBean getParent() {
        return parent;
    }

    public void setParent(DirectoryBean parent) {
        this.parent = parent;
    }
    
    void create(ArchiveDatabase db) {
        String id = db.getInsertObject().createDirectory(getDirName(), getDirParentId());
        setId(id);
    }
    
    @Override
    public void move(DirectoryBean newParent, ArchiveDatabase db) {
        final String newParentId = newParent.getId();
        db.getInsertObject().updateParentOfDirectory(this, newParentId);
        setDirParentId(newParentId);
    }
    
    @Override
    public void rename(String newName, ArchiveDatabase db) {
        db.getInsertObject().renameDirectory(this, newName);
        setDirName(newName);
    }

    @Override
    public String toString() {
        return "DirectoryBean{" + "id=" + id + ", dirName=" + dirName + 
                ", dirParentId=" + dirParentId + '}' +
                (getParent() != null ? "->Parent: " + getParent().toString() : "");
    }
}
