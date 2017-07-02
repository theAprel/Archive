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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Manages multiple <code>DirectoryStructure</code>. This is necessary for 
 * adding groups of files to the database because they may need to be inserted 
 * into multiple directories. It is also used for navigating through directories 
 * (e.g. by a GUI).
 * @author Aprel
 */
public class Directories {
    private final Map<String, DirectoryStructure> pathsToDirs;
    private final DirectoryBean catalogBean;
    private final LinkedList<DirectoryBean> basePath;
    private final LinkedList<DirectoryBean> newDirs = new LinkedList<>();
    private boolean canAcceptFiles = true;
    private Collection<DirectoryStructure> structures;
    private final ArchiveDatabase db;
    
    private static final Logger LOG = LoggerFactory.getLogger(Directories.class);
    
    public Directories(String catalog, String path, ArchiveDatabase db) 
            throws CatalogDoesNotExistException {
        path = DirectoryStructure.sanitizePath(path);
        this.db = db;
        catalogBean = DirectoryStructure.getCatalog(catalog, db);
        pathsToDirs = new HashMap<>();
        basePath = new LinkedList<>();
        basePath.add(catalogBean);
        final String[] pathParts = path.split("/");
        //create the base path. Note: files cannot be added into the base path
        if(pathParts[0].length() != 0) { // == 0 when path is just the root dir, i.e. ""
            for(String s : pathParts) {
                if(s.length() == 0) {
                    String msg = "Path contains zero-length directories.";
                    LOG.error(msg + " {} {}", path, pathParts);
                    throw new IllegalArgumentException(msg);
                }
                DirectoryBean nextDir = DirectoryStructure.getDir(s, basePath.getLast().getId(), db);
                if(nextDir == null) {
                    String msg = "Base path contains a directory not in the archive: " + s;
                    LOG.error(msg + " {} {}", path, pathParts);
                    throw new IllegalArgumentException(msg);
                }
                basePath.add(nextDir);
            }
        }
    }
    
    /**
     * Adds files to the end of this <code>Directories</code>' base path.
     * @param files 
     * @param localBasePath 
     */
    public void addFiles(Collection<FileBean> files, String localBasePath) {
        if(files.isEmpty()) {
            LOG.warn("Asked to add an empty collection of files");
            return;
        }
        if(!canAcceptFiles)
            throw new IllegalStateException("This object has already added or is"
                    + " pending to add files to the database. You must commit "
                    + "files and then create a new object.");
        //first create DirectoryStructures for each diversion off of path
        Multimap<String,FileBean> pathToBean = HashMultimap.create();
        files.stream().forEach(f -> {
            pathToBean.put(stripLastElementOffPath(f.getPath()), f);
        });
        
        final Set<String> keys = pathToBean.keySet();
        final Set<String> pathsThatMustExist = new HashSet<>();
        keys.stream().forEach(key -> {
            pathsThatMustExist.addAll(getAllSubPaths(key));
        });
        final List<String> orderedPaths = pathsThatMustExist.stream()
                .filter(k -> k.length() > 0) //filter out the root path: ""
                .sorted(new Comparator<String>() {
            private final CharMatcher matcher = CharMatcher.is('/');
            @Override
            public int compare(String o1, String o2) {
                return matcher.countIn(o1) - matcher.countIn(o2);
            }
        }).collect(Collectors.toList());
        final Map<String,DirectoryBean> pathKeyDirValue = new HashMap<>();
        pathKeyDirValue.put("", basePath.getLast()); //add root of relative path
        orderedPaths.stream().forEachOrdered(relativePath -> { //make DirectoryStructures
            final String base = stripLastElementOffPath(relativePath);
            final String name = getLastElementOfPath(relativePath);
            final DirectoryBean parentBean = pathKeyDirValue.get(base);
            DirectoryBean dirBean;
            if(newDirs.parallelStream().anyMatch(x -> x == parentBean)) {
                //this directory's parent also has not been created yet in db
                //so this directory must be new as well
                dirBean = createDirectory(name, parentBean);
            }
            else {
                dirBean = DirectoryStructure.getDir(name, parentBean.getId(), db);
                if(dirBean == null) {
                    //see if it's in the db; else, create it
                    dirBean = createDirectory(name, parentBean);
                }
            }
            pathKeyDirValue.put(relativePath, dirBean);
        });
        final Map<String,DirectoryStructure> pathToDirectoryStructure = new HashMap<>();
        pathToDirectoryStructure.put("", new DirectoryStructure(basePath.getLast(), db));
        orderedPaths.stream().forEachOrdered(relativePath -> {
            pathToDirectoryStructure.put(relativePath, 
                    new DirectoryStructure(pathKeyDirValue.get(relativePath), db));
        });
        //then, line up which files are going into which dirs
        pathToBean.asMap().forEach((k, v) -> pathToDirectoryStructure.get(k).addFiles(v));
        
        //then, make sure each that no 2 files share a name outside (i.e. to be added) or inside the archive
        //^--The DirectoryStructure class handles this
        
        //then, set the localStoragePath field for each file, as well as other relevant fields
        pathToBean.forEach((relativePath, bean) -> bean.setLocalStoragePath(
                localBasePath + (relativePath.length() == 0 ? "" : "/") + relativePath 
                        + "/" + bean.getFilename()));
        
        //all done! Ready to be committed to the database by another method
        canAcceptFiles = false;
        structures = pathToDirectoryStructure.values();
    }
    
    public void commitToDatabase() {
        if(canAcceptFiles)
            throw new IllegalStateException("No files have been added");
        //must create the missing db directory; order is critical because of references
        //and care was taking in the addFiles method
        newDirs.forEach(d -> {
            d.create(db);
            LOG.info("Created directory: {}", d);
        });
        structures.forEach(DirectoryStructure::commitToDatabase);
    }
    
    /**
     * Does not check that directory already exists in the database.
     * This must be checked externally to this method.
     * Does not actually create the directory in the database, but simply
     * queues its creation.
     */
    private DirectoryBean createDirectory(String name, DirectoryBean parentBean) {
        DirectoryBean child = new DirectoryBean();
        child.setDirName(name);
        child.setParent(parentBean);
        newDirs.add(child);
        return child;
    }
    
    private static String stripLastElementOffPath(String path) {
        final int endIndex = path.lastIndexOf("/");
        //no "/" separator == file is at root
        return endIndex == -1 ? "" : path.substring(0, endIndex);
    }
    
    private static String getLastElementOfPath(String path) {
        final int lastIndex = path.lastIndexOf("/");
        if(lastIndex == -1)
            return path;
        return path.substring(lastIndex+1);
    }
    
    private static Set<String> getAllSubPaths(String path) {
        Set<String> res = new HashSet<>();
        getAllSubPathRecursiveImpl(path, res);
        return res;
    }
    
    private static void getAllSubPathRecursiveImpl(String recurString, Set<String> set) {
        set.add(recurString);
        if(!recurString.contains("/"))
            return; //all done
        else {
            getAllSubPathRecursiveImpl(stripLastElementOffPath(recurString), set);
        }
    }
    
    public List<String> getDirectoriesToBeCreated() {
        newDirs.stream().forEach(d -> LOG.debug("To be created: {}", d));
        return newDirs.stream().map(DirectoryBean::getDirName).collect(Collectors.toList());
    }
}
