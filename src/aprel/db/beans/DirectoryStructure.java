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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final DirectoryBean thisDir;
    /**
     * Take care to add dirs to list in the order that they should be created.
     */
    private final List<FileBean> newFiles;
    private final List<FileBean> files;
    private final List<DirectoryBean> directories;
    private static final Table<String,String,DirectoryBean> CACHE_DIR = HashBasedTable.create();
    
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryStructure.class);
    
    DirectoryStructure(DirectoryBean dir, ArchiveDatabase db) {
        this.db = db;
        handle = db.getHandle();
        thisDir = dir;
        newFiles = new ArrayList<>();
        if(thisDir.existsInDatabase()) {
            files = db.getQueryObject().getAllFilesInDirectoryBesidesOtherDirectories(thisDir.getId());
            directories = db.getQueryObject().getAllDirectoriesInDirectory(thisDir.getId());
        }
        else {
            files = new ArrayList<>();
            directories = new ArrayList<>();
        }
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
    
    /**
     * Queue files to be added to the database.
     * Check for filename duplicates but not db md5 duplicates.
     * @param files 
     */
    void addFiles(Collection<FileBean> files) {
        //test the input first
        if(files.isEmpty()) {
            LOG.warn("Asked to add an empty collection of files to this directory");
            return;
        }
        //any duplicate names in the collection provided?
        Set<String> names = files.parallelStream().map(FileBean::getFilename)
                .collect(Collectors.toSet());
        if(names.size() != files.size())
            throw new IllegalArgumentException(
                    "The provided collection contains files with the same name");
        //any files with the same name in this directory?
        names.retainAll(this.files);
        //names is now useless; do not use it past the following conditional:
        if(!names.isEmpty())
            throw new IllegalArgumentException(
                    "Duplicate names with files in directory " + thisDir + ": " + names);
        //all tests are done; now queue the files to be added
        newFiles.addAll(files);
    }
    
    /**
     * Inserts the files into the db, trusting that any missing directories have 
     * already been created externally.
     * 
     * This method takes the return values of the insertion (i.e. the fileIds) 
     * and sets the corresponding FileBean. However, the JDBIv2 library is limited 
     * in that it can only return these values as an int[]. This can lead to 
     * integer overflow and incorrect fileIds being set. If any part of the rest 
     * of the code base requires knowing these ids for newly-inserted files, this 
     * method will have to be re-written to not use a batch insert or another way 
     * to get the ids as Strings from the library will be necessary.
     */
    void commitToDatabase() {
        //List<String> ids = db.getInsertObject().insertAllNoMetadata(newFiles.iterator());
        int[] ids = db.getInsertObject().insertAllNoMetadata(newFiles.iterator());
        if(ids.length != newFiles.size()) 
            throw new IllegalStateException("DB returned a different number of IDs"
                    + " from files inserted: " + ids.length + " != " + newFiles.size());
        Iterator<Integer> idIterator = Ints.asList(ids).iterator();
        Iterator<FileBean> beanIterator = newFiles.iterator();
        while(idIterator.hasNext())
            beanIterator.next().setId(idIterator.next()+"");
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
