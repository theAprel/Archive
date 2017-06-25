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

import aprel.jdbi.Insert;
import aprel.jdbi.Query;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Aprel
 */
public class ArchiveDatabase {
    private final String user, pass, sqlServer, dbName;
    private final DBI dbi;
    private final Handle h;
    private Query query;
    private Insert insert;
    
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveDatabase.class);
    
    private static final String SQL_SERVER_URL_PREFIX = "jdbc:mysql://";

    public ArchiveDatabase(String user, String pass, String server, String dbName) throws SQLException {
        this.user = user;
        this.pass = pass;
        sqlServer = server;
        this.dbName = dbName;
        dbi = new DBI(SQL_SERVER_URL_PREFIX + sqlServer +
                (sqlServer.endsWith("/") ? "" : "/") + this.dbName,
                this.user, this.pass);
        h = dbi.open();
    }
    
    public Query getQueryObject() {
        if(query == null)
            query = h.attach(Query.class);
        return query;
    }
    
    public Insert getInsertObject() {
        if(insert == null)
            insert = h.attach(Insert.class);
        return insert;
    }
    
    public Handle getHandle() {
        return h;
    }
    
    public void close() throws SQLException {
        h.close();
    }
    
    public static ArchiveDatabase createDefaultDatabase() throws 
            ParserConfigurationException, SAXException, IOException, SQLException {
        InputStream credStream = ArchiveDatabase.class.getResourceAsStream(
                "/aprel/db-credentials.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(credStream);
        String user = doc.getElementsByTagName("user").item(0).getTextContent();
        String pass = doc.getElementsByTagName("password").item(0).getTextContent();
        String server = doc.getElementsByTagName("server").item(0).getTextContent();
        String db = doc.getElementsByTagName("database").item(0).getTextContent();
        return new ArchiveDatabase(user, pass, server, db);
    }
}
