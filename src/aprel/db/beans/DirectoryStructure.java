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
import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Aprel
 */
public class DirectoryStructure {
    private final String path;
    private final ArchiveDatabase db;
    private final Handle handle;
    private final DirectoryBean catalogBean;
    private final List<DirectoryBean> beanPath;
    
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryStructure.class);
    
    public DirectoryStructure(String catalog, String path, ArchiveDatabase db) 
            throws CatalogDoesNotExistException {
        path = path.startsWith("/") ? path.substring(1) : path;
        final String[] pathParts = path.split("/");
        for(String s : pathParts) {
            if(s.length() == 0) {
                String msg = "Path contains zero-length directories.";
                LOG.error(msg, path, pathParts);
                throw new IllegalArgumentException(msg);
            }
        }
        this.path = path;
        this.db = db;
        handle = db.getHandle();
        catalogBean = db.getQueryObject().getCatalog(catalog);
        if(catalogBean == null)
            throw new CatalogDoesNotExistException(catalog);
        beanPath = new ArrayList<>();
        beanPath.add(catalogBean);
        for(String dir : pathParts) {
            String parentId = beanPath.get(beanPath.size()-1).getId();
            DirectoryBean bean = getDir(dir, parentId);
            if(bean == null) {
                LOG.info("Directory does not exist in archive: " + dir);
                if(shouldCreateDirectory(dir))
                    createDirectory(dir, parentId);
                else
                    throw new IllegalStateException("Directory does not exist and should not be created: " + dir);
            }
            beanPath.add(bean);
        }
    }
    
    private DirectoryBean getDir(String name, String dirParentId) {
        return db.getQueryObject().getDirectory(name, dirParentId);
    }
    
    private boolean directoryExists(String name, String dirParentId) {
        return getDir(name, dirParentId) == null;
    }
    
    private boolean shouldCreateDirectory(String name) {
        Console console = System.console();
        if(console == null) {
            LOG.warn("No system console. Cowardly refusing to create a new directory.");
            return false;
        }
        for(;;) {
            System.out.println("Directory \"" + name + "\" does not exist. Should it be created? Y/N");
            String response = console.readLine().toUpperCase();
            switch(response) {
                case "Y": return true;
                case "N": return false;
                default: System.out.println("Try again.");
            }
        }
    }
    
    /**
     * Does not check whether directory already exists.
     * @param dirName
     * @param parentDirId
     * @return 
     */
    private DirectoryBean createDirectory(String dirName, String parentDirId) {
        LOG.debug("Creating directory " + dirName + " with parent " + parentDirId);
        String id = db.getInsertObject().createDirectory(dirName, parentDirId);
        if(id == null) {
            throw new IllegalStateException("Did not receive new serial id when creating dir");
        }
        DirectoryBean bean = new DirectoryBean();
        bean.setId(id);
        bean.setDirName(dirName);
        bean.setDirParentId(parentDirId);
        LOG.debug("Directory created:", bean);
        return bean;
    }
}
