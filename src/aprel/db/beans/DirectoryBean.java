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
import java.util.Objects;

/**
 *
 * @author Aprel
 */
public class DirectoryBean {
    private String id, dirName, dirParentId;
    private DirectoryBean parent;
    private boolean idHasBeenSet = false;

    public boolean existsInDatabase() {
        return idHasBeenSet;
    }
    
    public String getId() {
        if(!idHasBeenSet)
            throw new IllegalStateException("getId that has not been set");
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
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.id);
        hash = 31 * hash + Objects.hashCode(this.dirName);
        hash = 31 * hash + Objects.hashCode(this.dirParentId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DirectoryBean other = (DirectoryBean) obj;
        if(this.id == null || other.id == null)
            throw new IllegalStateException("Comparing an instance with uninitializwed ID");
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "DirectoryBean{" + "id=" + id + ", dirName=" + dirName + 
                ", dirParentId=" + dirParentId + '}' +
                (getParent() != null ? "->Parent: " + getParent().toString() : "");
    }
}
