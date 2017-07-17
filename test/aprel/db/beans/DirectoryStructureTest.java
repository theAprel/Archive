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
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
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
        when(db.getDeleteObject()).thenReturn(delete);
        
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
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        Set testContents = Sets.newHashSet(dir1, dir2, file1, file2, file3);
        List<DbFile> files = ds.getFiles();
        assertTrue(testContents.containsAll(files));
        assertTrue(files.containsAll(testContents));
    }

    /**
     * Test of getNamesInDir method, of class DirectoryStructure.
     */
    @Test
    public void testGetNamesInDir() {
        System.out.println("getNamesInDir");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        Set<DbFile> testContents = Sets.newHashSet(dir1, dir2, file1, file2, file3);
        Set<String> testNames = testContents.stream().map(DbFile::getName).collect(Collectors.toSet());
        Collection<String> actual = ds.getNamesInDir();
        assertTrue(actual.size() == testNames.size());
        assertTrue(testNames.containsAll(actual));
    }

    /**
     * Test of getInnerDirectoryByName method, of class DirectoryStructure.
     */
    @Test
    public void testGetInnerDirectoryByName() {
        System.out.println("getInnerDirectoryByName");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertEquals(ds.getInnerDirectoryByName(dir1.getDirName()), dir1);
        assertEquals(ds.getInnerDirectoryByName(dir2.getDirName()), dir2);
    }
    
    @Test
    public void testGetInnerDirectoryByNameGivenFile() {
        System.out.println("getInnerDirectoryByNameGivenFile");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertNull(ds.getInnerDirectoryByName(file1.getFilename()));
    }
    
    @Test
    public void testGetInnerDirectoryByNameDoesNotExist() {
        System.out.println("getInnerDirectoryByNameNotExist");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertNull(ds.getInnerDirectoryByName("doesnotexist"));
    }

    /**
     * Test of getInnerDirectories method, of class DirectoryStructure.
     */
    @Test
    public void testGetInnerDirectories() {
        System.out.println("getInnerDirectories");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        Collection<DirectoryBean> actual = ds.getInnerDirectories();
        Set<DirectoryBean> expected = Sets.newHashSet(dir1, dir2);
        assertTrue(expected.size() == actual.size());
        assertTrue(actual.containsAll(expected));
    }

    /**
     * Test of getName method, of class DirectoryStructure.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        String name = thisDir.getDirName();
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertEquals(name, ds.getName());
    }

    /**
     * Test of deleteChildDirectory method, of class DirectoryStructure.
     */
    @Test
    public void testDeleteChildDirectory_String() {
        System.out.println("deleteChildDirectory");
        FileBean f = new FileBean();
        f.setId("5464");
        f.setFilename("a real file");
        when(query.getAllFilesInDirectoryBesidesOtherDirectories(dir1.getId()))
                .thenReturn(Collections.singletonList(f));
        DirectoryStructure shouldNotDelete = new DirectoryStructure(thisDir, db);
        assertFalse(shouldNotDelete.deleteChildDirectory(dir1.getDirName()));
        
        //mock returns empty lists for unmocked methods
        DirectoryStructure shouldDelete = new DirectoryStructure(thisDir, db);
        assertTrue(shouldDelete.deleteChildDirectory(dir2.getDirName()));
        verify(delete).deleteDir(dir2);
        verifyNoMoreInteractions(delete);
        verifyZeroInteractions(ins);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteChildDirectory_StringThrowsException() {
        System.out.println("deleteChildDirectoryThrowsException");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        ds.deleteChildDirectory("doesnotexist");
    }

    /**
     * Test of deleteChildDirectory method, of class DirectoryStructure.
     */
    @Test
    public void testDeleteChildDirectory_DirectoryBean() {
        System.out.println("deleteChildDirectory");
        FileBean f = new FileBean();
        f.setId("5464");
        f.setFilename("a real file");
        when(query.getAllFilesInDirectoryBesidesOtherDirectories(dir1.getId()))
                .thenReturn(Collections.singletonList(f));
        DirectoryStructure shouldNotDelete = new DirectoryStructure(thisDir, db);
        assertFalse(shouldNotDelete.deleteChildDirectory(dir1));
        
        //mock returns empty lists for unmocked methods
        DirectoryStructure shouldDelete = new DirectoryStructure(thisDir, db);
        assertTrue(shouldDelete.deleteChildDirectory(dir2));
        verify(delete).deleteDir(dir2);
        verifyNoMoreInteractions(delete);
        verifyZeroInteractions(ins);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteChildDirectory_DirectoryBeanThrowsException() {
        System.out.println("deleteChildDirectoryThrowsException");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        DirectoryBean fake = new DirectoryBean();
        fake.setDirName("fakefakefake");
        fake.setDirParentId("1337");
        fake.setId("1447");
        ds.deleteChildDirectory(fake);
    }

    /**
     * Test of isEmpty method, of class DirectoryStructure.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        DirectoryStructure isNotEmpty = new DirectoryStructure(thisDir, db);
        assertFalse(isNotEmpty.isEmpty());
        
        DirectoryStructure isEmpty = new DirectoryStructure(dir1, db);
        assertTrue(isEmpty.isEmpty());
    }

    /**
     * Test of canAccept method, of class DirectoryStructure.
     */
    @Test
    public void testCanAccept() {
        System.out.println("canAccept");
        FileBean f = new FileBean();
        f.setFilename("a new name");
        DirectoryBean d = new DirectoryBean();
        d.setDirName("a new directory");
        FileBean same = new FileBean();
        same.setFilename(file2.getFilename());
        Collection<DbFile> nope = Lists.newArrayList(f, d, same);
        Collection<DbFile> yep = Lists.newArrayList(f, d);
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertFalse(ds.canAccept(nope));
        assertTrue(ds.canAccept(yep));
        assertFalse(ds.canAccept(same));
        assertTrue(ds.canAccept(f));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCanAcceptCollectionHasDups() {
        System.out.println("canAcceptCollectionHasDups");
        String name = "the same name";
        FileBean f = new FileBean();
        f.setFilename(name);
        FileBean f2 = new FileBean();
        f2.setFilename(name);
        
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertFalse(ds.canAccept(Lists.newArrayList(f, f2)));
    }
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();


    /**
     * Test of moveTo method, of class DirectoryStructure.
     */
    @Test
    public void testMoveTo_DirectoryStructure() {
        System.out.println("moveTo");
        DirectoryStructure biological = new DirectoryStructure(thisDir, db);
        DirectoryBean beanAdoptiveDir = new DirectoryBean();
        beanAdoptiveDir.setDirName("adopt");
        beanAdoptiveDir.setId("143");
        beanAdoptiveDir.setDirParentId("134");
        FileBean sameNameAsFile2 = new FileBean();
        sameNameAsFile2.setFilename(file2.getFilename());
        sameNameAsFile2.setDirParentId(beanAdoptiveDir.getId());
        when(query.getAllFilesInDirectoryBesidesOtherDirectories(beanAdoptiveDir.getId()))
                .thenReturn(Lists.newArrayList(sameNameAsFile2));
        DirectoryStructure adoptive = new DirectoryStructure(beanAdoptiveDir, db);
        
        final int originalSize = biological.getFiles().size();
        List<DbFile> movedFiles = Lists.newArrayList(dir1, file1);
        biological.moveTo(adoptive, movedFiles);
        
        movedFiles.forEach(f -> {
            assertFalse(biological.getFiles().contains(f));
            assertTrue(adoptive.getFiles().contains(f));
            assertTrue(biological.getFiles().containsAll(Lists.newArrayList(dir2, file2, file3)));
            assertEquals(originalSize - 2, biological.getFiles().size());
            assertEquals(2+1, adoptive.getFiles().size());
        });
        
        verify(ins).updateParentOfDirectory(dir1, beanAdoptiveDir.getId());
        verify(ins).updateParentOfFile(file1, beanAdoptiveDir.getId());
        verifyNoMoreInteractions(ins);
        verifyZeroInteractions(delete);
        
        exception.expect(IllegalArgumentException.class);
        biological.moveTo(adoptive, Lists.newArrayList(file2));
    }
    
    @Test
    public void testMoveTo_DirectoryStructure_MoveIntoItself() {
        DirectoryStructure biological = new DirectoryStructure(thisDir, db);
        DirectoryStructure adoptive = new DirectoryStructure(dir1, db);
        verifyZeroInteractions(ins);
        verifyZeroInteractions(delete);
        exception.expect(IllegalArgumentException.class);
        biological.moveTo(adoptive, dir1);
    }
    
    @Test
    public void testMoveTo_DirectoryStructure_NotTheParent() {
        DirectoryStructure biological = new DirectoryStructure(thisDir, db);
        DirectoryStructure adoptive = new DirectoryStructure(dir1, db);
        FileBean unrelated = new FileBean();
        unrelated.setDirParentId("989898");
        verifyZeroInteractions(ins);
        verifyZeroInteractions(delete);
        exception.expect(IllegalArgumentException.class);
        biological.moveTo(adoptive, unrelated);
    }

    /**
     * Test of rename method, of class DirectoryStructure.
     */
    @Test
    public void testRename() {
        System.out.println("rename");
        DirectoryStructure ds = new DirectoryStructure(thisDir, db);
        assertTrue(ds.rename(dir1, "a totally novel directory name"));
        assertTrue(ds.rename(file1, "a totally original file name"));
        assertFalse(ds.rename(file2, file3.getFilename()));
        assertFalse(ds.rename(file2, dir2.getName()));
        verify(ins).renameDirectory(dir1, "a totally novel directory name");
        verify(ins).renameFile(file1, "a totally original file name");
        verifyNoMoreInteractions(ins);
        
        FileBean unrelated = new FileBean();
        unrelated.setDirParentId("48978788");
        exception.expect(IllegalArgumentException.class);
        ds.rename(unrelated, "ahahahahahahahah");
    }
    
}
