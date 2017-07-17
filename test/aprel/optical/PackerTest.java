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
package aprel.optical;

import aprel.db.beans.FileBean;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Aprel
 */
@RunWith(value = Parameterized.class)
public class PackerTest {
    
    final Packer packer;
    
    Optical opt1;
    Optical opt2;
    List<Optical> startingOpticals;
    
    @Before
    public void setUp() {
        opt1 = new Optical();
        opt2 = new Optical();
        FileBean parent = new FileBean();
        parent.setId("15698");
        parent.setFilename("the first parent");
        parent.setSize(Optical.PAYLOAD_SPACE + Optical.PAYLOAD_SPACE/2);
        Part fillOpt1 = new Part();
        fillOpt1.setParent(parent);
        fillOpt1.setSize(Optical.PAYLOAD_SPACE);
        opt1.add(fillOpt1);
        Part partiallyFillOpt2 = new Part();
        partiallyFillOpt2.setParent(parent);
        partiallyFillOpt2.setSize(Optical.PAYLOAD_SPACE/2);
        opt2.add(partiallyFillOpt2);
        startingOpticals = Lists.newArrayList(opt1, opt2);
    }
    
    public PackerTest(Packer toTest) {
        packer = toTest;
    }
    
    static List<FileBean> createFiles() {
        FileBean f1 = new FileBean();
        f1.setFilename("one");
        f1.setSize(50L * 1000L * 1000L);
        
        FileBean f2 = new FileBean();
        f2.setFilename("two");
        f2.setSize(100L * 1000L * 1000L);
        
        return Lists.newArrayList(f1, f2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxGreaterThanSizeThrowsException() {
        packer.packFilesIntoOpticals(createFiles(), 1, startingOpticals);
    }
    
    @Test
    public void testReallyBigFile() {
        FileBean reallyBigFile = new FileBean();
        reallyBigFile.setSize(200L * 1000L * 1000L * 1000L);
        List<Optical> result = packer.packFilesIntoOpticals(Lists.newArrayList(reallyBigFile), 
                Integer.MAX_VALUE, startingOpticals);
        List<Part> inOpt2 = checkStartingOpticals(result);
        List<Optical> sublist = result.subList(2, result.size());
        inOpt2.addAll(sublist.stream().flatMap(o -> o.getParts().stream()).collect(Collectors.toList()));
        checkPartsForFile(inOpt2, reallyBigFile);
    }
    
    @Test
    public void testGroupOfFiles() {
        long[] megabytesPerFile = new long[] {
            65L, 1L, 3L, 3556L, 49275L, 89214L, 8945L, 9987L, 124L, 14965L, 16481L
        };
        List<FileBean> files = new ArrayList<>();
        for(long i : megabytesPerFile) {
            FileBean f = new FileBean();
            f.setSize(i * 1000L * 1000L);
            f.setFilename(i + " megabyte-sized file");
            files.add(f);
        }
        List<Optical> result = packer.packFilesIntoOpticals(files, Integer.MAX_VALUE, startingOpticals);
        List<Part> allParts = checkStartingOpticals(result);
        List<Optical> sublist = result.subList(2, result.size());
        allParts.addAll(sublist.stream().flatMap(o -> o.getParts().stream())
                .collect(Collectors.toList()));
        if(allParts.stream().anyMatch(p -> p.getParent() == null || !files.contains(p.getParent())))
            fail();
        Map<FileBean, List<Part>> filesToParts = allParts.stream().collect(
                Collectors.groupingBy(Part::getParent, Collectors.mapping(
                        Function.identity(), Collectors.toList())));
        filesToParts.forEach((f, ps) -> checkPartsForFile(ps, f));
    }
    
    private void checkPartsForFile(List<Part> parts, FileBean file) {
        long offset = 0L;
        final long fileSize = file.getSize();
        for(Part p : parts) {
            if(p.getOffset() < offset) fail();
            assertEquals(file, p.getParent());
            assertEquals(offset, p.getOffset());
            offset += p.getSize();
        }
        assertEquals(fileSize, offset);
    }
    
    private List<Part> checkStartingOpticals(List<Optical> result) {
        assertTrue(opt1 == result.get(0));
        assertTrue(opt2 == result.get(1));
        return opt2.getParts().subList(1, opt2.getParts().size());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() {
        Object[] obj = new Object[1];
        obj[0] = new SimplePacker();
        return Collections.singletonList(obj);
    }
    
}
