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
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Aprel
 */
public class DirectoryStructureTest {
    
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
        String name = "";
        DirectoryStructure instance = null;
        DirectoryBean expResult = null;
        DirectoryBean result = instance.createDirectory(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
