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
package aprel.xml.tags;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Aprel
 */
public enum WtvMetadata implements XmlTag {
    TITLE("Title"), 
    SUBTITLE("WM/SubTitle"), 
    DESCRIPTION("WM/SubTitleDescription"), 
    CHANNEL("service_name"), 
    ORIGINAL_BROADCAST_DATETIME("WM/MediaOriginalBroadcastDateTime"),
    ORIGINAL_RUNTIME("WM/MediaOriginalRunTime"),
    DURATION("Duration");
    
    private final String wtvKey;
    
    private static final Map<String,WtvMetadata> map = new HashMap<>(10);
    
    private WtvMetadata(String wtvKey) {
        this.wtvKey = wtvKey;
    }
    
    /**
     * ffprobe lists several key-value pairs for Wtv file metadata; returns the 
     * key for the corresponding relevant datum.
     * 
     * @return 
     */
    public String getWtvMetadataKey() {
        return wtvKey;
    }
    
    /**
     * 
     * @param key
     * @return The WtvMetadata associated with the key, or <code>null</code> 
     * if there is no such association.
     */
    public static WtvMetadata getFromWtvMetadataKey(String key) {
        if(map.isEmpty()) {
            //initialize
            for(WtvMetadata w : WtvMetadata.values())
                map.put(w.getWtvMetadataKey(), w);
        }
        return map.get(key);
    }

    @Override
    public String getXmlTag() {
        if(DURATION == this)
            throw new UnsupportedOperationException(DURATION + " has multiple XML tags");
        return name();
    }
}
