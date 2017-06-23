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
package aprel.tags.db;

import aprel.tags.xml.FileMetadata;
import aprel.tags.xml.WtvMetadata;
import aprel.tags.xml.Xml;
import aprel.tags.xml.XmlTag;

/**
 *
 * This is the one database table with no auto-increment id. It uses the id from 
 * the `files` table instead because all files have at most one metadata associated 
 * with them.
 * @author Aprel
 */
public enum Metadata implements TableTag {
    /*
    fileId (File serial ID) | title | subtitle | description | channel (service_name) | 
    originalBroadcast (datetime) | originalRuntime | duration100Nanos | duration (readable)
    */
    
    FILE_ID("fileId"),
    TITLE("title", WtvMetadata.TITLE),
    SUBTITLE("subtitle", WtvMetadata.SUBTITLE),
    DESCRIPTION("description", WtvMetadata.DESCRIPTION),
    CHANNEL("channel", WtvMetadata.CHANNEL),
    BROADCAST_ORIGINAL("originalBroadcast", WtvMetadata.ORIGINAL_BROADCAST_DATETIME),
    RUNTIME_ORIGINAL("originalRuntime", WtvMetadata.ORIGINAL_RUNTIME),
    DURATION_100_NANOS("duration100Nanos", FileMetadata.DURATION_100NANOS),
    DURATION_READABLE("duration", FileMetadata.DURATION_READABLE);
    
    private final String tableHeader;
    private final XmlTag corresponding;
    public static final String TABLE_NAME = "metadata";
    
    private Metadata(String tableHeader) {
        this(tableHeader, Xml.NO_XML);
    }
    
    private Metadata(String tableHeader, XmlTag xml) {
        this.tableHeader = tableHeader;
        corresponding = xml;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getColumnHeader() {
        return tableHeader;
    }

    @Override
    public XmlTag getCorrespondingXmlTag() {
        return corresponding;
    }
}
