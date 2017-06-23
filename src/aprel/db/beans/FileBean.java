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

import aprel.tags.db.TableTag;
import aprel.tags.xml.Attribute;
import aprel.tags.xml.FileMetadata;
import aprel.tags.xml.Xml;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Aprel
 */
public class FileBean {
    /*
    (Serial) id | filename | dirParentId | md5 | size | catalog
    | BOOL onOptical | BOOL onLocalDisc | localStoragePath
    */
    private String id, filename, dirParentId, md5, catalog, localStoragePath;
    private long size;
    private boolean onOptical, onLocalDisc;
    
    private static final Logger LOG = LoggerFactory.getLogger(FileBean.class);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDirParentId() {
        return dirParentId;
    }

    public void setDirParentId(String dirParentId) {
        this.dirParentId = dirParentId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    public void setLocalStoragePath(String localStoragePath) {
        this.localStoragePath = localStoragePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isOnOptical() {
        return onOptical;
    }

    public void setOnOptical(boolean onOptical) {
        this.onOptical = onOptical;
    }

    public boolean isOnLocalDisc() {
        return onLocalDisc;
    }

    public void setOnLocalDisc(boolean onLocalDisc) {
        this.onLocalDisc = onLocalDisc;
    }
    
    public static FileBean fromXmlElement(Element fileElement) {
        FileBean bean = new FileBean();
        String path = fileElement.getAttribute(Attribute.PATH.getXmlAttribute());
        String[] parts = path.split("/");
        String filename = parts[parts.length-1];
        bean.filename = filename;
        
        NodeList children = fileElement.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if(n.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element e = (Element) n;
            String tag = e.getTagName();
            String inner = e.getTextContent();
            TableTag tableTag = TableTag.getFromXml(tag);
            if(tableTag == null)
                throw new IllegalArgumentException("XML contains an unrecognized tag: " + tag);
            try {
                Field field = bean.getClass().getDeclaredField(tableTag.getColumnHeader());
                if(tag.equals(FileMetadata.SIZE.getXmlTag())) {
                    field.setLong(bean, Long.parseLong(inner));
                }
                else {
                    field.set(bean, inner);
                }
            } catch (NoSuchFieldException ex) {
                LOG.error("No such field", ex);
                return null;
            } catch (SecurityException ex) {
                LOG.error("Security settings prevent reflection", ex);
                return null;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                LOG.error("Error changing field value", ex);
                return null;
            }
        }
        return bean;
    }
    
    public static List<FileBean> fromXml(Document doc) {
        Element filesElement = doc.getDocumentElement();
        List<FileBean> beans = new ArrayList<>();
        NodeList children = filesElement.getElementsByTagName(Xml.FILE.getXmlTag());
        for(int i = 0; i < children.getLength(); i++) {
            Element e = (Element) children.item(i);
            beans.add(fromXmlElement(e));
        }
        return beans;
    }
}
