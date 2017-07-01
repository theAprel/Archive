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
import java.util.Collection;
import java.util.List;

/**
 *
 * A Packer takes a collection of files, chunks them into Parts, and packs the 
 * parts into Opticals. Each implementation can choose how best to accomplish 
 * this. In addition, implementations may choose not to pack certain files.
 * 
 * Packers must set the parent, size, and offset of the Parts they create, but 
 * the do not have to set the "ordinal" and "totalInSet" properties, which must 
 * be set externally.
 * 
 * @author Aprel
 */
public interface Packer {
    public List<Optical> packFilesIntoOpticals(Collection<FileBean> files);
}
