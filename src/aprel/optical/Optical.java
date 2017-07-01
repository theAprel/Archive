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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.hash.HashingOutputStream;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Not thread-safe.
 * @author Aprel
 */
public class Optical {
    private final List<Part> parts;
    private long spaceRemaining;
    
    public static final long MAX_BYTES = 25L * 1000L * 1000L * 1000L; //Bluray
    public static final int OVERHEAD = 5 * 1000 * 1000; // 5MB to be on the safe side
    public static final long PAYLOAD_SPACE = MAX_BYTES - OVERHEAD;
    
    public Optical() {
        parts = new ArrayList<>();
        spaceRemaining = PAYLOAD_SPACE;
    }
    
    public void add(Part p) {
        final long size = p.getSize();
        if(size > spaceRemaining)
            throw new IllegalArgumentException("Part exceeds available space: " 
                    + size + " > " + spaceRemaining);
        parts.add(p);
        spaceRemaining -= size;
    }
    
    public long getAvailableSpace() {
        return spaceRemaining;
    }
    
    public List<Part> getParts() {
        return new ArrayList<>(parts);
    }
    
    public void writePartsToDir(String dir) throws IOException {
        if(!new File(dir).isDirectory())
            throw new IllegalArgumentException("Not a directory: " + dir);
        dir += dir.endsWith("/") ? "" : "/";
        @SuppressWarnings("deprecation")
        final HashFunction md5 = Hashing.md5();
        for(Part p : parts) {
            final ByteSource offsetView = Files.asByteSource(new File(p.getParent()
                    .getLocalStoragePath())).slice(p.getOffset(), p.getSize());
            final HashingInputStream hashFromStream = new HashingInputStream(md5, offsetView.openStream());
            final ByteSink outSink = Files.asByteSink(new File(dir + p.getPartFilename()));
            final long written = outSink.writeFrom(hashFromStream);
            if(written != p.getSize())
                throw new IOException("Did not write all bytes from source to destination: "
                        + "Wrote " + written + " bytes out of " + p.getSize());
            p.setMd5(hashFromStream.hash().toString());
            hashFromStream.close();
        }
    }

    @Override
    public String toString() {
        return "Optical{" + "parts=" + parts + ", spaceRemaining=" + spaceRemaining + '}';
    }
}
