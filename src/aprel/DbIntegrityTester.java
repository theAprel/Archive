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
package aprel;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.skife.jdbi.v2.Handle;

/**
 *
 * @author Aprel
 */
public class DbIntegrityTester {
    
    private static ArchiveDatabase db;
    private static Handle h;
    
    public static void main(String[] args) throws Exception {
        db = ArchiveDatabase.createDefaultDatabase();
        h = db.getHandle();
        
        boolean allTestsPassed =
        assertTrue(checkDatabaseDirectoryStructure(), "Database Directory Structure Test");
        
        System.out.println();
        if(allTestsPassed) System.out.println("ALL TESTS PASSED");
        else System.out.println("SOME TESTS FAILED");
        
        db.close();
    }
    
    private static boolean checkDatabaseDirectoryStructure() {
        List<Map<String,Object>> dirQuery = h.select("SELECT id,dirParentId FROM directories");
        List<BigInteger> listDirIds = dirQuery.parallelStream().map(row -> row.get("id"))
                .map(o -> (BigInteger) o).collect(Collectors.toList());
        Set<BigInteger> dirIds = new HashSet<>(listDirIds);
        if(listDirIds.size() != dirIds.size()) {
            System.out.println("Database contains directories with duplicate ids");
            return false;
        }
        
        List<Map<String,Object>> parentQuery = h.select("SELECT dirParentId FROM files");
        Set<BigInteger> parentIds = Stream.concat(parentQuery.parallelStream(), dirQuery.parallelStream())
                .map(row -> row.get("dirParentId")).distinct().filter(o -> o != null)
                .map(o -> (BigInteger) o).collect(Collectors.toSet());
        if(!dirIds.containsAll(parentIds)) {
            System.out.println("Files have missing parent directories");
            parentIds.removeAll(dirIds);
            System.out.println("Missing directory ids: " + parentIds.toString());
            return false;
        }
        
        dirIds.removeAll(parentIds);
        if(!dirIds.isEmpty()) {
            System.out.println("Some directories are empty. This is not an error, "
                    + "but perhaps these directories should be removed");
            System.out.println("Empty directory ids: " + dirIds.toString());
        }
        
        return true;
    }
    
    private static boolean assertTrue(boolean condition, String testName) {
        System.out.print(testName + ": ");
        if(condition) System.out.println("SUCCESS");
        else System.out.println("FAILED");
        return condition;
    }
}
