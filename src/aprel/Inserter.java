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
package aprel;

import aprel.db.beans.FileBean;
import aprel.jdbi.Insert;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author Aprel
 */
public class Inserter {
    
    public Inserter(Document doc) {
        
    }
    
    public static void main(String[] args) throws Exception {
        String root = args[0];
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(root + (root.endsWith("/") ? "" : "/") + "METADATA.xml");
        List<FileBean> l = FileBean.fromXml(doc);
        //files are not on optical; they are on local storage
        l.stream().forEach(bean ->  {
            bean.setOnOptical(false);
            bean.setOnLocalDisc(true);
                });
        
        ArchiveDatabase db = ArchiveDatabase.createDefaultDatabase();
        Insert ins = db.getHandle().attach(Insert.class);
        ins.insertAllNoMetadata(l.iterator());
        
        db.close();
    }
}
