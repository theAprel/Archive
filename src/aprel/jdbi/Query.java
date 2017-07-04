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
package aprel.jdbi;

import aprel.db.beans.DirectoryBean;
import aprel.db.beans.FileBean;
import aprel.jdbi.beanmappers.DirectoryBeanMapper;
import aprel.jdbi.beanmappers.FileBeanMapper;
import aprel.jdbi.beanmappers.PartMapper;
import aprel.optical.Part;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

/**
 *
 * @author Aprel
 */
public interface Query {
    //DIRECTORY QUERIES
    @SqlQuery("SELECT * FROM directories WHERE dirParentId IS NULL")
    @Mapper(DirectoryBeanMapper.class)
    public List<DirectoryBean> getCatalogs();
    
    /**
     * Call DirectoryStructure.getCatalog
     * @param catalogName
     * @return 
     */
    @SqlQuery("SELECT * FROM directories WHERE dirParentId IS NULL AND dirName= :name")
    @Mapper(DirectoryBeanMapper.class)
    public DirectoryBean getCatalog(@Bind("name") String catalogName);
    
    @SqlQuery("SELECT * FROM directories WHERE dirParentId= :id")
    @Mapper(DirectoryBeanMapper.class)
    public List<DirectoryBean> getSubdirectories(@BindBean DirectoryBean parent);
    
    @SqlQuery("SELECT * FROM directories WHERE dirName= :dir AND dirParentId= :parent")
    @Mapper(DirectoryBeanMapper.class)
    public DirectoryBean getDirectory(@Bind("dir") String dirName, @Bind("parent") String parentDirId);
    
    @SqlQuery("SELECT * FROM directories WHERE dirParentId= :id")
    @Mapper(DirectoryBeanMapper.class)
    public List<DirectoryBean> getAllDirectoriesInDirectory(@Bind("id") String directoryId);
    
    //FILE QUERIES
    @SqlQuery("SELECT * FROM files WHERE dirParentId= :id")
    @Mapper(FileBeanMapper.class)
    public List<FileBean> getAllFilesInDirectoryBesidesOtherDirectories(@Bind("id") String directoryId);
    
    @SqlQuery("SELECT * FROM files WHERE md5= :md5")
    @Mapper(FileBeanMapper.class)
    public FileBean getByMd5(@Bind("md5") String md5sum);
    
    @SqlQuery("SELECT * FROM files WHERE NOT onOptical")
    @Mapper(FileBeanMapper.class)
    public List<FileBean> getAllFilesNotOnOptical();
    
    @SqlQuery("SELECT * FROM files WHERE NOT md5Verified")
    @Mapper(FileBeanMapper.class)
    public List<FileBean> getAllFilesNotMd5Verified();
    
    //PART-RELATED QUERIES
    @SqlQuery("SELECT MAX(discNumber) FROM parts WHERE catalog= :cat")
    public int getLastDiscNumberInSeries(@Bind("cat") String catalog);
    
    @Deprecated
    @SqlQuery("SELECT * FROM parts WHERE md5Verified")
    @Mapper(PartMapper.class)
    public List<Part> getMd5VerifiedParts();
    
}
