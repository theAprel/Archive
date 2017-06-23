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
import aprel.tags.xml.Xml;
import aprel.tags.xml.XmlTag;

/**
 *
 * @author Aprel
 */
public enum Files implements TableTag {
    /*
    (Serial) id | filename | dirParentId | md5 | size | catalog (i.e. optical-
    media set, for numbering, e.g. main, videos, anime, etc.) | BOOL onOptical |
    BOOL onLocalDisc | localStoragePath (tmp until written to optical, or has 
    been retrieved)
    */
    ID("id"),
    FILENAME("filename"),
    DIR_PARENT_ID("dirParentId"),
    MD5("md5", FileMetadata.MD5),
    SIZE("size", FileMetadata.SIZE),
    CATALOG("catalog"),
    ON_OPTICAL("onOptical"),
    ON_LOCAL_DISC("onLocalDisc"),
    LOCAL_STOTAGE_PATH("localStoragePath");
    
    private final String dbColumnHeader;
    private final XmlTag corresponding;
    public static final String TABLE_NAME = "files";
    
    private Files(String columnHeader) {
        this(columnHeader, Xml.NO_XML);
    }
    
    private Files(String columnHeader, XmlTag xmlTag) {
        dbColumnHeader = columnHeader;
        corresponding = xmlTag;
    }
    
    @Override
    public String getTableName() {
        return TABLE_NAME;
    }
    
    @Override
    public String getColumnHeader() {
        return dbColumnHeader;
    }

    @Override
    public XmlTag getCorrespondingXmlTag() {
        return corresponding;
    }
   
}
