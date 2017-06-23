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

import aprel.tags.xml.Xml;
import aprel.tags.xml.XmlTag;

/**
 *
 * @author Aprel
 */
public enum Directories implements TableTag {
    /*
    (Serial) id | dirName | dirParentId
    */
    
    ID("id"),
    DIR_NAME("dirName"),
    DIR_PARENT_ID("dirParentId");
    
    private final String tableHeader;
    public static final String TABLE_NAME = "directories";
    
    private Directories(String tableHeader) {
        this.tableHeader = tableHeader;
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
        return Xml.NO_XML;
    }
}
