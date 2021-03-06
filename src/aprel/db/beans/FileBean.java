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

import aprel.ArchiveDatabase;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Aprel
 */
@XmlRootElement( name = "FILE" )
@XmlType(propOrder = {"size", "md5", "media"} )
public class FileBean implements DbFile {
    /*
    (Serial) id | filename | dirParentId | md5 | size | catalog
    | BOOL onOptical | BOOL md5Verified | BOOL onLocalDisc | localStoragePath
    */
    private String id, path, filename, dirParentId, md5, catalog, localStoragePath;
    private long size;
    private boolean onOptical, onLocalDisc, md5Verified;
    private MediaMetadata media;
    private DirectoryBean parent;
    private boolean idHasBeenSet = false;
    
    private static final Logger LOG = LoggerFactory.getLogger(FileBean.class);

    public boolean existsInDatabase() {
        return idHasBeenSet;
    }
    
    /**
     * 
     * @return the id in database, or {@code null} if not in database.
     */
    @Override
    public String getId() {
        //it would be nice to have this check, but we can't b/c of JavaBeans
//        if(!idHasBeenSet)
//            throw new IllegalStateException("getId that has not been set");
        return id;
    }

    @XmlTransient
    public void setId(String id) {
        this.id = id;
        idHasBeenSet = true;
    }

    public String getFilename() {
        if(filename == null) {
            String[] parts = path.split("/");
            String name = parts[parts.length - 1];
            filename = name;
        }
        return filename;
    }
    
    @XmlTransient
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    @XmlTransient
    @Override
    public String getName() {
        return getFilename();
    }

    public String getPath() {
        return path;
    }
    
    @XmlAttribute( name = "path", required = true )
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getDirParentId() {
        if(parent != null)
            return parent.getId();
        return dirParentId;
    }

    @XmlTransient
    public void setDirParentId(String dirParentId) {
        this.dirParentId = dirParentId;
        parent = null;
    }

    public DirectoryBean getParent() {
        return parent;
    }

    @XmlTransient
    public void setParent(DirectoryBean parent) {
        this.parent = parent;
    }

    public String getMd5() {
        return md5;
    }

    @XmlElement( name = "MD5", required = true )
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public String getCatalog() {
        return catalog;
    }
    
    @XmlTransient
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    @XmlTransient
    public void setLocalStoragePath(String localStoragePath) {
        this.localStoragePath = localStoragePath;
    }

    public long getSize() {
        return size;
    }

    @XmlElement( name = "SIZE", required = true )
    public void setSize(long size) {
        this.size = size;
    }

    public boolean isOnOptical() {
        return onOptical;
    }

    @XmlTransient
    public void setOnOptical(boolean onOptical) {
        this.onOptical = onOptical;
    }

    public boolean isOnLocalDisc() {
        return onLocalDisc;
    }

    @XmlTransient
    public void setOnLocalDisc(boolean onLocalDisc) {
        this.onLocalDisc = onLocalDisc;
    }

    public MediaMetadata getMedia() {
        return media;
    }

    @XmlElement( name = "MEDIA" )
    public void setMedia(MediaMetadata media) {
        this.media = media;
    }
    
    public boolean hasMediaData() {
        return media != null;
    }

    public boolean isMd5Verified() {
        return md5Verified;
    }

    @XmlTransient
    public void setMd5Verified(boolean md5Verified) {
        this.md5Verified = md5Verified;
    }

    @Override
    public void move(DirectoryBean newParent, ArchiveDatabase db) {
        db.getInsertObject().updateParentOfFile(this, newParent.getId());
        setParent(newParent);
    }

    @Override
    public void rename(String newName, ArchiveDatabase db) {
        db.getInsertObject().renameFile(this, newName);
        setFilename(newName);
    }
    
    public boolean queryMetadata(ArchiveDatabase db) {
        if(getMedia() == null)
            setMedia(db.getQueryObject().getMetadata(getId()));
        return getMedia() != null;
    }

    @Override
    public String toString() {
        return "FileBean{" + "id=" + id + ", path=" + path + ", filename=" + 
                filename + ", dirParentId=" + dirParentId + ", md5=" + md5 + 
                ", localStoragePath=" + localStoragePath + 
                ", size=" + size + ", onOptical=" + onOptical + ", onLocalDisc=" + 
                onLocalDisc + ", media=" + media + '}';
    }
    
    @XmlType(propOrder = {"title", "subtitle", "description", "channel",
    "originalBroadcast", "originalRuntime", "duration100Nanos", "duration"} )
    public static class MediaMetadata {
        private String title, subtitle, description, channel, originalRuntime,
                duration100Nanos, duration, originalBroadcast;
        //private Instant originalBroadcast;

        public String getTitle() {
            return title;
        }

        @XmlElement( name = "TITLE" )
        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        @XmlElement( name = "SUBTITLE" )
        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getDescription() {
            return description;
        }

        @XmlElement( name = "DESCRIPTION" )
        public void setDescription(String description) {
            this.description = description;
        }

        public String getChannel() {
            return channel;
        }

        @XmlElement( name = "CHANNEL" )
        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getOriginalRuntime() {
            return originalRuntime;
        }

        @XmlElement( name = "ORIGINAL_RUNTIME" )
        public void setOriginalRuntime(String originalRuntime) {
            this.originalRuntime = originalRuntime;
        }

        public String getDuration100Nanos() {
            return duration100Nanos;
        }

        @XmlElement( name = "DURATION_100NANOS" )
        public void setDuration100Nanos(String duration100Nanos) {
            this.duration100Nanos = duration100Nanos;
        }

        public String getDuration() {
            return duration;
        }

        @XmlElement( name = "DURATION_READABLE" )
        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getOriginalBroadcast() {
            return originalBroadcast;
        }

        @XmlElement( name = "ORIGINAL_BROADCAST_DATETIME" )
        public void setOriginalBroadcast(String originalBroadcast) {
            this.originalBroadcast = originalBroadcast;
        }
        
        public String prettyPrint() {
            StringBuilder builder = new StringBuilder();
            if(getTitle() != null)
                builder.append("Title: ").append(getTitle()).append("\n");
            if(getSubtitle() != null)
                builder.append("Subtitle: ").append(getSubtitle()).append("\n");
            if(getDescription() != null)
                builder.append("Description: ").append(getDescription()).append("\n");
            if(getChannel() != null)
                builder.append("Channel: ").append(getChannel()).append("\n");
            if(getOriginalRuntime() != null)
                builder.append("Original Runtime: ").append(getOriginalRuntime()).append("\n");
            if(getDuration100Nanos() != null)
                builder.append("Duration (100-ns units): ").append(getDuration100Nanos())
                        .append("\n");
            if(getDuration() != null)
                builder.append("Duration (readable): ").append(getDuration()).append("\n");
            if(getOriginalBroadcast() != null)
                builder.append("Original Broadcast Date: ").append(getOriginalBroadcast())
                        .append("\n");
            return builder.toString();
        }

        @Override
        public String toString() {
            return "MediaMetadata{" + "title=" + title + ", subtitle=" + subtitle + 
                    ", description=" + description + ", channel=" + channel + 
                    ", originalRuntime=" + originalRuntime + ", duration100Nanos=" + 
                    duration100Nanos + ", duration=" + duration + 
                    ", originalBroadcast=" + originalBroadcast + '}';
        }
    }
}
