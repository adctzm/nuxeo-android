/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ecm.automation.client.jaxrs.model;

import java.util.Date;

import org.json.JSONObject;

import android.util.Log;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class PropertiesHelper {

    public static boolean isBlob(Object v) {
        return v instanceof Blob;
    }

    public static boolean isMap(Object v) {
        return v instanceof PropertyMap;
    }

    public static boolean isList(Object v) {
        return v instanceof PropertyList;
    }

    public static boolean isScalar(Object v) {
        return !isBlob(v) && !isList(v) && !isMap(v);
    }

    public static String getString(Object v, String defValue) {
        if (v == null) {
            return defValue;
        }
        if (v.getClass() == String.class) {
            return v.toString();
        }
        throw new IllegalArgumentException("Property is not a scalar: " + v);
    }

    public static Boolean getBoolean(Object v, Boolean defValue) {
        if (v == null) {
            return defValue;
        }
        if (v.getClass() == String.class) {
            return Boolean.valueOf(v.toString());
        }
        throw new IllegalArgumentException("Property is not a scalar: " + v);
    }

    public static Long getLong(Object v, Long defValue) {
        if (v == null) {
            return defValue;
        }
        if (v.getClass() == String.class) {
            return Long.valueOf(v.toString());
        }
        throw new IllegalArgumentException("Property is not a scalar: " + v);
    }

    public static Double getDouble(Object v, Double defValue) {
        if (v == null) {
            return defValue;
        }
        if (v.getClass() == String.class) {
            return Double.valueOf(v.toString());
        }
        throw new IllegalArgumentException("Property is not a scalar: " + v);
    }

    public static Date getDate(Object v, Date defValue) {
        if (v == null || "null".equals(v)) {
            return defValue;
        }
        if (v.getClass() == String.class) {
        	// XXX 
        	String dateString = v.toString().substring(0, 10);
            return DateUtils.parseDate(dateString);
        }
        throw new IllegalArgumentException("Property is not a scalar: " + v);
    }

    public static PropertyList getList(Object v, PropertyList defValue) {
        if (v == null) {
            return defValue;
        }
        if (v instanceof PropertyList) {
            return (PropertyList) v;
        }
        throw new IllegalArgumentException("Property is not a list: " + v);
    }

    public static PropertyMap getMap(Object v, PropertyMap defValue) {
        if (v == null) {
            return defValue;
        }
        if (v instanceof PropertyMap) {
            return (PropertyMap) v;
        }
        throw new IllegalArgumentException("Property is not a map: " + v);
    }

    public static String toStringProperties(PropertyMap props) {
        StringBuffer sb = new StringBuffer();
        for (String propName : props.getKeys()) {
            Object value = props.map().get(propName);
            if (value != null) {
                sb.append(propName);
                sb.append("=");
                sb.append(encodePropertyAsString(value));
                sb.append("\n");
            } else {
                Log.w(PropertiesHelper.class.getSimpleName(), "No value for "
                        + propName);
            }
        }
        return sb.toString();
    }

    public static String encodePropertyAsString(Object prop) {
        if (prop instanceof PropertyList) {
            PropertyList list = (PropertyList) prop;
            StringBuffer sb = new StringBuffer();
            for (Object item : list.list()) {
                sb.append(encodePropertyAsString(item));
                sb.append(";");
            }
            return sb.toString();
        } else if (prop instanceof PropertyMap) {
            PropertyMap map = (PropertyMap) prop;
            return new JSONObject(map.map()).toString();
        } else {
            String value = prop.toString();
            return value.replace("\n", " \\\n");
        }
    }
}
