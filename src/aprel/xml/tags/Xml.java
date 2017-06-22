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

/**
 *
 * @author Aprel
 */
public enum Xml implements XmlTag {
    ROOT("FILES"), FILE("FILE");
    
    private final String tag;
    
    private Xml(String tag) {
        this.tag = tag;
    }
    
    @Override
    public String getXmlTag() {
        return tag;
    }
}
