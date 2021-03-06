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
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Not thread-safe.
 * @author Aprel
 */
public class Optical {
    protected final List<Part> parts;
    protected long spaceRemaining;
    
    public static final long MAX_BYTES = 25_025_314_816L; //Bluray
    public static final int OVERHEAD = 2 * 1000 * 1000;
    public static final long PAYLOAD_SPACE = MAX_BYTES - OVERHEAD;
    @SuppressWarnings("deprecation")
    private static final HashFunction MD5 = Hashing.md5();
    
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
        
        for(Part p : parts) {
            final ByteSource offsetView = Files.asByteSource(new File(p.getParent()
                    .getLocalStoragePath())).slice(p.getOffset(), p.getSize());
            InputStream fromStream;
            if(p.getMd5() == null)
                fromStream = new HashingInputStream(MD5, offsetView.openStream());
            else
                fromStream = offsetView.openStream();
            final ByteSink outSink = Files.asByteSink(new File(dir + p.getPartFilename()));
            final long written = outSink.writeFrom(fromStream);
            if(written != p.getSize())
                throw new IOException("Did not write all bytes from source to destination: "
                        + "Wrote " + written + " bytes out of " + p.getSize());
            if(p.getMd5() == null)
                p.setMd5(((HashingInputStream)fromStream).hash().toString());
            fromStream.close();
        }
    }

    @Override
    public String toString() {
        return "Optical{" + "parts=" + parts + ", spaceRemaining=" + spaceRemaining + '}';
    }
}
