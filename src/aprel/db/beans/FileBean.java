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

import aprel.tags.xml.Xml;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Aprel
 */
@XmlRootElement( name = "FILE" )
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

    @XmlAttribute( name = "path", required = true )
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

    @XmlElement( name = "MD5" )
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

    @XmlElement( name = "SIZE" )
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

    @Override
    public String toString() {
        return "FileBean{" + "id=" + id + ", filename=" + filename + 
                ", dirParentId=" + dirParentId + ", md5=" + md5 + ", catalog=" +
                catalog + ", localStoragePath=" + localStoragePath + ", size=" +
                size + ", onOptical=" + onOptical + ", onLocalDisc=" + onLocalDisc + '}';
    }
    
    public static List<FileBean> fromXml(Document doc) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(FileBean.class);
        Unmarshaller unmarsh = jaxbContext.createUnmarshaller();
        
        Element filesElement = doc.getDocumentElement();
        List<FileBean> beans = new ArrayList<>();
        NodeList children = filesElement.getElementsByTagName(Xml.FILE.getXmlTag());
        for(int i = 0; i < children.getLength(); i++) {
            FileBean b = (FileBean) unmarsh.unmarshal(children.item(i));
            //strip filename off from path
            String path = b.filename;
            String[] parts = path.split("/");
            String filename = parts[parts.length - 1];
            b.filename = filename;
            beans.add(b);
        }
        return beans;
    }
}
