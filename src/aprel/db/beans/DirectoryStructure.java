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
import org.skife.jdbi.v2.Handle;

/**
 *
 * @author Aprel
 */
public class DirectoryStructure {
    private final String path;
    private final ArchiveDatabase db;
    private final Handle handle;
    private final DirectoryBean catalogBean;
    
    public DirectoryStructure(String catalog, String path, ArchiveDatabase db) 
            throws CatalogDoesNotExistException {
        this.path = path;
        this.db = db;
        handle = db.getHandle();
        catalogBean = db.getQueryObject().getCatalog(catalog);
        if(catalogBean == null)
            throw new CatalogDoesNotExistException(catalog);
        System.out.println(catalogBean);
    }
}
