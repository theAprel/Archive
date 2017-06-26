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
import com.google.common.base.CharMatcher;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Aprel
 */
public class DirectoryStructure {
    private final ArchiveDatabase db;
    private final Handle handle;
    private final DirectoryBean catalogBean;
    private final LinkedList<DirectoryBean> beanPath;
    /**
     * Take care to add dirs to list in the order that they should be created.
     */
    private final List<FileBean> newFiles;
    private static final Table<String,String,DirectoryBean> CACHE_DIR = HashBasedTable.create();
    
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryStructure.class);
    
    DirectoryStructure(DirectoryBean catalog, List<DirectoryBean> path, ArchiveDatabase db) {
        this.beanPath = new LinkedList<>(path);
        this.db = db;
        handle = db.getHandle();
        catalogBean = catalog;
        if(beanPath.get(0) != catalogBean) {
            LOG.debug("Catalog not at the start of directory path. Appended to start.");
            beanPath.addFirst(catalog);
        }
        newFiles = new ArrayList<>();
//        for(String dir : pathParts) {
//            DirectoryBean parentBean = beanPath.get(beanPath.size()-1);
//            String parentId = parentBean.getId();
//            DirectoryBean bean = getDir(dir, parentId);
//            if(bean == null) {
//                LOG.debug("Directory does not exist in archive: " + dir);
//                bean = new DirectoryBean();
//                bean.setDirName(dir);
//                bean.setParent(parentBean);
//                newDirectories.add(bean);
//            }
//            beanPath.add(bean);
//        }
    }
    
    static String sanitizePath(String path) {
        if(path == null || path.equals(""))
            throw new IllegalArgumentException("Path is empty or null");
        path = path.startsWith("/") ? path.substring(1) : path;
        path = path.endsWith("/") ? path.substring(0, path.length()-1) : path;
        return path;
    }
    
    static DirectoryBean getCatalog(String name, ArchiveDatabase db) 
            throws CatalogDoesNotExistException {
        DirectoryBean bean = db.getQueryObject().getCatalog(name);
        if(bean == null)
            throw new CatalogDoesNotExistException(name);
        return bean;
    }
    
    public void addFiles(Collection<FileBean> files) {
        if(files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Cannot add a list of empty files");
        }
        DirectoryBean relativeRoot = beanPath.get(beanPath.size()-1);
        int longestPath = files.parallelStream().mapToInt(f -> {
            return CharMatcher.is('/').countIn(f.getPath());
        }).max().getAsInt();
    }
    
    static DirectoryBean getDir(String name, String dirParentId, ArchiveDatabase db) {
        DirectoryBean cachedCopy = CACHE_DIR.get(name, dirParentId);
        if(cachedCopy != null)
            return cachedCopy;
        DirectoryBean bean = db.getQueryObject().getDirectory(name, dirParentId);
        if(bean != null)
            CACHE_DIR.put(name, dirParentId, bean);
        return bean;
    }
    
    /**
     * Does not check whether directory already exists.
     * @param dirName
     * @param parentDirId
     * @return 
     */
//    private DirectoryBean createDirectory(String dirName, String parentDirId) {
//        LOG.debug("Creating directory " + dirName + " with parent " + parentDirId);
//        String id = db.getInsertObject().createDirectory(dirName, parentDirId);
//        if(id == null) {
//            throw new IllegalStateException("Did not receive new serial id when creating dir");
//        }
//        DirectoryBean bean = new DirectoryBean();
//        bean.setId(id);
//        bean.setDirName(dirName);
//        bean.setDirParentId(parentDirId);
//        LOG.debug("Directory created:", bean);
//        return bean;
//    }
}
