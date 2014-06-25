/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.jsonpath.internal.spi.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.internal.Utils;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.json.Mode;

import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.scripts.JO;
import jdk.nashorn.internal.runtime.ScriptObject;

/**
 * A JsonProvider that can work with native JS objects and Arrays in Nashorn.
 * 
 * @author Will Tran <wtran@pivotallabs.com>
 *
 */
@SuppressWarnings("restriction")
public class NashornJsonProvider implements JsonProvider {

    @Override
    public Object parse(String json) throws InvalidJsonException {
        return jdk.nashorn.internal.runtime.JSONFunctions.parse(json, null);
    }

    @Override
    public Object parse(Reader jsonReader) throws InvalidJsonException {
        try {
            return parse(IOUtils.toString(jsonReader));
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public Object parse(InputStream jsonStream) throws InvalidJsonException {
        try {
            return parse(IOUtils.toString(jsonStream));
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public String toJson(Object obj) {
        return jdk.nashorn.internal.objects.NativeJSON.stringify(null, obj, null, null).toString();
    }

    @Override
    public Object createMap() {
        return new JO(jdk.nashorn.internal.runtime.PropertyMap.newMap());
    }

    @Override
    public Object createArray() {
        return NativeArray.construct(false, null);
    }

    @Override
    public Object clone(Object model) {
        return Utils.clone((Serializable) model);
    }

    @Override
    public boolean isContainer(Object obj) {
        return isArray(obj) || isMap(obj);
    }

    @Override
    public boolean isArray(Object obj) {
        return obj instanceof NativeArray;
    }

    @Override
    public int length(Object obj) {
        return ((ScriptObject) obj).size();
    }

    @Override
    public Iterable<Object> toIterable(Object obj) {
        return new Iterable<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return ((ScriptObject) obj).valueIterator();
            }
        };
    }

    @Override
    public Collection<String> getPropertyKeys(Object obj) {
        Collection<String> keys = new ArrayList<String>();
        ((ScriptObject) obj).propertyIterator().forEachRemaining(k -> keys.add(k));
        return keys;
    }

    @Override
    public Object getProperty(Object obj, Object key) {
        return ((ScriptObject) obj).get(key);
    }

    @Override
    public void setProperty(Object obj, Object key, Object value) {
        ((ScriptObject) obj).set(key, value, false);
    }

    @Override
    public boolean isMap(Object obj) {
        return obj instanceof ScriptObject && !((ScriptObject) obj).isArray();
    }

    @Override
    public Mode getMode() {
        return Mode.STRICT;
    }

    @Override
    public Object getArrayIndex(Object obj, int idx) {
        return ((ScriptObject) obj).get(idx);
    }

    @Override
    public Object getMapValue(Object obj, String key) {
        return ((ScriptObject) obj).get(key);
    }

    @Override
    public Object getMapValue(Object obj, String key, boolean signalUndefined) {
        Object result = getMapValue(obj, key);
        if (result == null && signalUndefined) {
            return JsonProvider.UNDEFINED;
        }
        return result;
    }

}
