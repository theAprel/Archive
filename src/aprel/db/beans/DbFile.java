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

/**
 *
 * @author Aprel
 */
public interface DbFile {
    public String getName();
    public String getId();
    /**
     * USE {@code DirectoryStructure.moveTo} INSTEAD!
     * 
     * Moves this file to a new parent, changing its internal properties and 
     * database representation to reflect the new parent. However, this does not 
     * perform any checks on the validity of this move with the file-structure 
     * integrity; that onus falls on the code that calls this method. 
     * @param newParent
     * @param db 
     */
    public void move(DirectoryBean newParent, ArchiveDatabase db);
    /**
     * USE {@code DirectoryStructure.rename} INSTEAD!
     * 
     * @param newName 
     * @param db 
     */
    public void rename(String newName, ArchiveDatabase db);
}
