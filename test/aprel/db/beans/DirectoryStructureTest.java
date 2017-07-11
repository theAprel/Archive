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
import aprel.jdbi.Delete;
import aprel.jdbi.Insert;
import aprel.jdbi.Query;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author Aprel
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectoryStructureTest {
    
    @Mock
    ArchiveDatabase db;
    
    @Mock
    Insert ins;
    
    @Mock
    Delete delete;
    
    @Mock
    Query query;
    
    DirectoryBean dir1, dir2, thisDir;
    FileBean file1, file2, file3;
    
    public DirectoryStructureTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        thisDir = new DirectoryBean();
        thisDir.setDirName("thisone");
        thisDir.setDirParentId("256");
        thisDir.setId("512");
        
        dir1 = new DirectoryBean();
        dir1.setDirName("firstdir");
        dir1.setDirParentId(thisDir.getId());
        dir1.setId("775");
        
        dir2 = new DirectoryBean();
        dir2.setDirName("another");
        dir2.setDirParentId(thisDir.getId());
        dir2.setId("777");
        
        file1 = new FileBean();
        file1.setCatalog("here");
        file1.setDirParentId(thisDir.getId());
        file1.setFilename("afile111");
        file2 = new FileBean();
        file2.setCatalog("here");
        file2.setDirParentId(thisDir.getId());
        file2.setFilename("afile121");
        file3 = new FileBean();
        file3.setCatalog("here");
        file3.setDirParentId(thisDir.getId());
        file3.setFilename("33afil");
        
        List<DirectoryBean> dirs = Lists.newArrayList(dir1, dir2);
        List<FileBean> files = Lists.newArrayList(file1, file2, file3);
        
        when(db.getInsertObject()).thenReturn(ins);
        when(db.getQueryObject()).thenReturn(query);
        
        when(query.getAllFilesInDirectoryBesidesOtherDirectories(thisDir.getId())).thenReturn(files);
        when(query.getAllDirectoriesInDirectory(thisDir.getId())).thenReturn(dirs);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createDirectory method, of class DirectoryStructure.
     */
    @Test
    public void testCreateDirectory() {
        System.out.println("createDirectory");
        final String id = thisDir.getId();
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        final String newName = "a new name no one has thought of before";
        when(ins.createDirectory(newName, id)).thenReturn("1865");
        DirectoryBean result = ds.createDirectory(newName);
        assertTrue(result.getDirName().equals(newName));
        assertTrue(result.getDirParentId().equals(id));
        assertTrue(result.getId().equals("1865"));
        verify(ins).createDirectory(newName, id);
        verifyNoMoreInteractions(ins);
        verifyZeroInteractions(delete);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDirectoryDuplicateFile() {
        System.out.println("createDirectoryDuplicateFile");
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        String oldName = file2.getFilename();
        DirectoryBean result = ds.createDirectory(oldName);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDirectoryDuplicateDirName() {
        System.out.println("createDirectoryDuplicateDirName");
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        String oldName = dir2.getDirName();
        DirectoryBean result = ds.createDirectory(oldName);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDirectoryEmptyString() {
        System.out.println("createDirectoryEmptyString");
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        ds.createDirectory("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateDirectoryNullString() {
        System.out.println("createDirectoryNullString");
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        ds.createDirectory(null);
    }

    /**
     * Test of getFiles method, of class DirectoryStructure.
     */
    @Test
    public void testGetFiles() {
        System.out.println("getFiles");
        DirectoryStructure instance = null;
        List<DbFile> expResult = null;
        List<DbFile> result = instance.getFiles();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNamesInDir method, of class DirectoryStructure.
     */
    @Test
    public void testGetNamesInDir() {
        System.out.println("getNamesInDir");
        DirectoryStructure instance = null;
        List<String> expResult = null;
        List<String> result = instance.getNamesInDir();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInnerDirectoryByName method, of class DirectoryStructure.
     */
    @Test
    public void testGetInnerDirectoryByName() {
        System.out.println("getInnerDirectoryByName");
        String name = "";
        DirectoryStructure instance = null;
        DirectoryBean expResult = null;
        DirectoryBean result = instance.getInnerDirectoryByName(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInnerDirectories method, of class DirectoryStructure.
     */
    @Test
    public void testGetInnerDirectories() {
        System.out.println("getInnerDirectories");
        DirectoryStructure instance = null;
        List<DirectoryBean> expResult = null;
        List<DirectoryBean> result = instance.getInnerDirectories();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class DirectoryStructure.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        DirectoryStructure instance = null;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteChildDirectory method, of class DirectoryStructure.
     */
    @Test
    public void testDeleteChildDirectory_String() {
        System.out.println("deleteChildDirectory");
        String name = "";
        DirectoryStructure instance = null;
        boolean expResult = false;
        boolean result = instance.deleteChildDirectory(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteChildDirectory method, of class DirectoryStructure.
     */
    @Test
    public void testDeleteChildDirectory_DirectoryBean() {
        System.out.println("deleteChildDirectory");
        DirectoryBean child = null;
        DirectoryStructure instance = null;
        boolean expResult = false;
        boolean result = instance.deleteChildDirectory(child);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEmpty method, of class DirectoryStructure.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        DirectoryStructure instance = null;
        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sanitizePath method, of class DirectoryStructure.
     */
    @Test
    public void testSanitizePath() {
        System.out.println("sanitizePath");
        String path = "";
        String expResult = "";
        String result = DirectoryStructure.sanitizePath(path);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCatalog method, of class DirectoryStructure.
     */
    @Test
    public void testGetCatalog() throws Exception {
        System.out.println("getCatalog");
        String name = "";
        ArchiveDatabase db = null;
        DirectoryBean expResult = null;
        DirectoryBean result = DirectoryStructure.getCatalog(name, db);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addFiles method, of class DirectoryStructure.
     */
    @Test
    public void testAddFiles() {
        System.out.println("addFiles");
        Collection<FileBean> files = null;
        DirectoryStructure instance = null;
        instance.addFiles(files);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canAccept method, of class DirectoryStructure.
     */
    @Test
    public void testCanAccept_Collection() {
        System.out.println("canAccept");
        Collection<? extends DbFile> files = null;
        DirectoryStructure instance = null;
        boolean expResult = false;
        boolean result = instance.canAccept(files);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canAccept method, of class DirectoryStructure.
     */
    @Test
    public void testCanAccept_DbFile() {
        System.out.println("canAccept");
        DbFile file = null;
        DirectoryStructure instance = null;
        boolean expResult = false;
        boolean result = instance.canAccept(file);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of moveTo method, of class DirectoryStructure.
     */
    @Test
    public void testMoveTo_DirectoryStructure_Collection() {
        System.out.println("moveTo");
        DirectoryStructure otherDirectory = null;
        Collection<DbFile> children = null;
        DirectoryStructure instance = null;
        instance.moveTo(otherDirectory, children);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of moveTo method, of class DirectoryStructure.
     */
    @Test
    public void testMoveTo_DirectoryStructure_DbFile() {
        System.out.println("moveTo");
        DirectoryStructure otherDirectory = null;
        DbFile file = null;
        DirectoryStructure instance = null;
        instance.moveTo(otherDirectory, file);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rename method, of class DirectoryStructure.
     */
    @Test
    public void testRename() {
        System.out.println("rename");
        DbFile child = null;
        String newName = "";
        DirectoryStructure instance = null;
        boolean expResult = false;
        boolean result = instance.rename(child, newName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commitToDatabase method, of class DirectoryStructure.
     */
    @Test
    public void testCommitToDatabase() {
        System.out.println("commitToDatabase");
        DirectoryStructure instance = null;
        instance.commitToDatabase();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDir method, of class DirectoryStructure.
     */
    @Test
    public void testGetDir() {
        System.out.println("getDir");
        String name = "";
        String dirParentId = "";
        ArchiveDatabase db = null;
        DirectoryBean expResult = null;
        DirectoryBean result = DirectoryStructure.getDir(name, dirParentId, db);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
