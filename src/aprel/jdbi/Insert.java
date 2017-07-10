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
import aprel.optical.Part;
import java.util.Iterator;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

/**
 *
 * @author Aprel
 */
public interface Insert {
    /**
     * Don't use this; it is buggy and may not return IDs. It throws a 
     * java.lang.IllegalArgumentException: Unable generate keys for a not prepared batch
     * @param beans
     * @return 
     */
    @Deprecated
    @SqlBatch("INSERT INTO files (filename, dirParentId, md5, size, catalog, onOptical, onLocalDisc, localStoragePath) "
            + "VALUES (:filename, :dirParentId, :md5, :size, :catalog, :onOptical, :onLocalDisc, :localStoragePath)")
    @GetGeneratedKeys
    public int[] insertAllNoMetadata(@BindBean Iterator<FileBean> beans);
    
    @SqlUpdate("INSERT INTO files (filename, dirParentId, md5, size, catalog, onOptical, onLocalDisc, localStoragePath) "
            + "VALUES (:filename, :dirParentId, :md5, :size, :catalog, :onOptical, :onLocalDisc, :localStoragePath)")
    @GetGeneratedKeys
    public String insertFile(@BindBean FileBean bean);
    
    @SqlUpdate("INSERT INTO metadata (fileId, title, subtitle, description, channel, originalBroadcast, originalRuntime, duration100Nanos, duration) "
            + "VALUES (:fileId, :title, :subtitle, :description, :channel, :originalBroadcast, :originalRuntime, :duration100Nanos, :duration)")
    public void insertMetadata(@Bind("fileId") String fileId, @BindBean FileBean.MediaMetadata metadata);
    
    @SqlUpdate("INSERT INTO directories (dirName) VALUES (:name)")
    public void createCatalog(@Bind("name") String name);
    
    /**
     * 
     * @param dirName
     * @param dirParentId
     * @return the id generated by the database for the newly-created directory
     */
    @SqlUpdate("INSERT INTO directories (dirName, dirParentId) VALUES (:dir, :parent)")
    @GetGeneratedKeys
    public String createDirectory(@Bind("dir") String dirName, @Bind("parent") String dirParentId);
    
    @SqlUpdate("INSERT INTO parts (partFilename, parentFileId, ordinal, totalInSet, md5, size, onOptical, md5Verified, catalog, discNumber, localStoragePath) "
            + "VALUES (:partFilename, :parentFileId, :ordinal, :totalInSet, :md5, :size, :onOptical, :md5Verified, :catalog, :discNumber, :localStoragePath)")
    @GetGeneratedKeys
    public String insertPart(@BindBean Part p);
    
    @SqlBatch("UPDATE files SET onOptical=1 WHERE id= :id")
    public void updateFilesOnOptical(@Bind("id") List<String> fileIds);
    
    @SqlBatch("UPDATE parts SET md5Verified=1 WHERE id= :id")
    public void updateVerifiedParts(@Bind("id") Iterator<String> partIds);
    
    @SqlBatch("UPDATE files SET md5Verified=1 WHERE id= :id")
    public void updateFullyVerifiedFiles(@Bind("id") Iterator<String> fileIds);
    
    @SqlBatch("UPDATE files SET dirParentId= :parent WHERE id= :id")
    public void updateParentOfFile(@BindBean FileBean file, @Bind("parent") String newParentId);
    
    @SqlBatch("UPDATE directories SET dirParentId= :parent WHERE id= :id")
    public void updateParentOfDirectory(@BindBean DirectoryBean dir, @Bind("parent") String newParentId);
}
