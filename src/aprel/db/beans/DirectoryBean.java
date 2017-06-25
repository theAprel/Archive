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

/**
 *
 * @author Aprel
 */
public class DirectoryBean {
    private String id, dirName, dirParentId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getDirParentId() {
        return dirParentId;
    }

    public void setDirParentId(String dirParentId) {
        this.dirParentId = dirParentId;
    }

    @Override
    public String toString() {
        return "DirectoryBean{" + "id=" + id + ", dirName=" + dirName + 
                ", dirParentId=" + dirParentId + '}';
    }
}
