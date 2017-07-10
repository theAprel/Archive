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
import aprel.jdbi.Insert;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    
    public DirectoryStructure(DirectoryBean dir, ArchiveDatabase db) {
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
    
    /**
     * Creates a new directory. If a file already exists by the given name, 
     * throws <code>IllegalArgumentException</code>.
     * @param name
     * @return 
     */
    public DirectoryBean createDirectory(String name) {
        if(name == null || name.equals(""))
            throw new IllegalArgumentException("Illegal directory name");
        final Set<String> filenames = new HashSet<>();
        final List<List<FileBean>> beans = new ArrayList<>();
        beans.add(files);
        beans.add(newFiles);
        beans.forEach((l) -> {
            filenames.addAll(l.parallelStream().map(FileBean::getFilename)
                    .collect(Collectors.toSet()));
        });
        filenames.addAll(directories.parallelStream()
                .map(DirectoryBean::getDirName).collect(Collectors.toSet()));
        if(filenames.contains(name))
            throw new IllegalArgumentException(name + " already exists in this directory");
        DirectoryBean newDir = new DirectoryBean();
        newDir.setDirName(name);
        newDir.setParent(thisDir);
        newDir.create(db);
        directories.add(newDir);
        return newDir;
    }
    
    public List<DbFile> getFiles() {
        if(!newFiles.isEmpty())
            throw new IllegalStateException("Files waiting to be committed to database");
        List<DbFile> fs = new ArrayList<>(files.size() + directories.size() + 1);
        fs.addAll(files);
        fs.addAll(directories);
        return fs;
    }
    
    public List<String> getNamesInDir() {
        List<DbFile> files = getFiles();
        return files.stream().map(DbFile::getName).collect(Collectors.toList());
    }
    
    /**
     * 
     * @param name
     * @return The <code>DirectoryBean</code> with the given name, or 
     * <code>null</code>.
     */
    public DirectoryBean getInnerDirectoryByName(String name) {
        return directories.parallelStream().filter(d -> d.getDirName()
                .equals(name)).findAny().orElse(null);
    }
    
    public List<DirectoryBean> getInnerDirectories() {
        return new ArrayList<>(directories);
    }
    
    public String getName() {
        return thisDir.getDirName();
    }
    
    /**
     * Deletes this directory if and only if it is empty.
     * 
     * If this method returns {@code true}, any further use of this object is 
     * undefined.
     * @return Whether this was deleted.
     */
    private boolean delete() {
        if(!isEmpty())
            return false;
        thisDir.delete(db);
        return true;
    }
    
    /**
     * Deletes a child directory if and only if it is empty.
     * 
     * If this method returns {@code true}, any further use of this object is 
     * undefined.
     * @param name
     * @return Whether this was deleted.
     */
    public boolean deleteChildDirectory(String name) {
        DirectoryBean child = directories.parallelStream()
                .filter(d -> d.getName().equals(name)).findAny().orElse(null);
        if(child == null)
            throw new IllegalArgumentException("No child directory by name " + name);
        return deleteChildDirectory(child);
    }
    
    public boolean deleteChildDirectory(DirectoryBean child) {
        if(!directories.contains(child))
            throw new IllegalArgumentException("No such child directory: " + child);
        DirectoryStructure ds = new DirectoryStructure(child, db);
        boolean deleted = ds.delete();
        if(deleted)
            directories.remove(child);
        return deleted;
    }
    
    public boolean isEmpty() {
        return files.isEmpty() && directories.isEmpty() && newFiles.isEmpty();
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
        if(!canAccept(files))
            throw new IllegalArgumentException("Duplicate names with files in directory " + thisDir);
        //all tests are done; now queue the files to be added
        newFiles.addAll(files);
    }
    
    public boolean canAccept(Collection<? extends DbFile> files) {
        //any duplicate names in the collection provided?
        Set<String> names = files.parallelStream().map(DbFile::getName)
                .collect(Collectors.toSet());
        if(names.size() != files.size())
            throw new IllegalArgumentException(
                    "The provided collection contains files with the same name");
        //any files or directories with the same name in this directory?
        final int startSize = names.size();
        names.removeAll(this.files.stream().map(FileBean::getFilename)
                .collect(Collectors.toSet()));
        names.removeAll(directories.stream().map(DirectoryBean::getDirName)
                .collect(Collectors.toSet()));
        //names is now useless; do not use it past the following conditional:
        return names.size() == startSize;
    }
    
    public boolean canAccept(DbFile file) {
        return canAccept(Collections.singleton(file));
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
     * 
     * This method now forgoes @SqlBatch and properly receives the ids as Strings.
     */
    void commitToDatabase() {
        Insert ins = db.getInsertObject();
        newFiles.forEach(bean -> {
            bean.setDirParentId(thisDir.getId());
            LOG.debug("Inserting {}", bean);
            String id = ins.insertFile(bean);
            LOG.debug("ID: {}", id);
            bean.setId(id);
            if(bean.hasMediaData()) {
                FileBean.MediaMetadata media = bean.getMedia();
                LOG.debug("File has multimedia data. Inserting into db: {}", media);
                ins.insertMetadata(id, media);
            }
        });
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
