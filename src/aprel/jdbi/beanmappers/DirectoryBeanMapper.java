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
package aprel.jdbi.beanmappers;

import aprel.db.beans.DirectoryBean;
import org.skife.jdbi.v2.BeanMapper;

/**
 *
 * @author Aprel
 */
public class DirectoryBeanMapper extends BeanMapper<DirectoryBean> {

    public DirectoryBeanMapper() {
        super(DirectoryBean.class);
    }
    
}
