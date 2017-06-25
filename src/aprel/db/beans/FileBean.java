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

import java.util.Objects;
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
public class FileBean {
    /*
    (Serial) id | filename | dirParentId | md5 | size
    | BOOL onOptical | BOOL onLocalDisc | localStoragePath
    */
    private String id, path, filename, dirParentId, md5, localStoragePath;
    private long size;
    private boolean onOptical, onLocalDisc;
    private MediaMetadata media;
    private DirectoryBean parent;
    
    private static final Logger LOG = LoggerFactory.getLogger(FileBean.class);

    public String getId() {
        return id;
    }

    @XmlTransient
    public void setId(String id) {
        this.id = id;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + Objects.hashCode(this.path);
        hash = 59 * hash + Objects.hashCode(this.dirParentId);
        hash = 59 * hash + Objects.hashCode(this.md5);
        hash = 59 * hash + (int) (this.size ^ (this.size >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileBean other = (FileBean) obj;
        if(this.id == null || other.id == null)
            throw new IllegalStateException("Comparing an instance with uninitializwed ID");
        return Objects.equals(this.id, other.id);
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
