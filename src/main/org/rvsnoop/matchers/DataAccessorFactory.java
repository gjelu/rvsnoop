/*
 * Class:     DataAccessorFactory
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.rvsnoop.NLSUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A factory for creating data accessors.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class DataAccessorFactory {

    static { NLSUtils.internationalize(DataAccessorFactory.class); }

    private static DataAccessorFactory instance;

    static String ERROR_BAD_IDENTIFIER, ERROR_BAD_NAME;

    public static synchronized DataAccessorFactory getInstance() {
        if (instance == null) { instance = new DataAccessorFactory(); }
        return instance;
    }

    private final Map<String, Class<? extends DataAccessor<?>>> identifiersToAccessorsMap = new LinkedHashMap<String, Class<? extends DataAccessor<?>>>();

    private final Map<String, Class<? extends DataAccessor<?>>> namesToAccessorsMap = new LinkedHashMap<String, Class<? extends DataAccessor<?>>>();

    private DataAccessorFactory() {
        identifiersToAccessorsMap.put(DataAccessor.FieldContents.IDENTIFIER, DataAccessor.FieldContents.class);
        identifiersToAccessorsMap.put(DataAccessor.FieldNames.IDENTIFIER, DataAccessor.FieldNames.class);
        identifiersToAccessorsMap.put(DataAccessor.ReplySubject.IDENTIFIER, DataAccessor.ReplySubject.class);
        identifiersToAccessorsMap.put(DataAccessor.SendSubject.IDENTIFIER, DataAccessor.SendSubject.class);
        identifiersToAccessorsMap.put(DataAccessor.TrackingId.IDENTIFIER, DataAccessor.TrackingId.class);
        namesToAccessorsMap.put(DataAccessor.FIELD_CONTENTS, DataAccessor.FieldContents.class);
        namesToAccessorsMap.put(DataAccessor.FIELD_NAMES, DataAccessor.FieldNames.class);
        namesToAccessorsMap.put(DataAccessor.REPLY_SUBJECT, DataAccessor.ReplySubject.class);
        namesToAccessorsMap.put(DataAccessor.SEND_SUBJECT, DataAccessor.SendSubject.class);
        namesToAccessorsMap.put(DataAccessor.TRACKING_ID, DataAccessor.TrackingId.class);
    }

    public DataAccessor<String> createFieldContentsAccessor() {
        return new DataAccessor.FieldContents();
    }

    public DataAccessor<String> createFieldNamesAccessor() {
        return new DataAccessor.FieldNames();
    }

    public DataAccessor<String> createReplySubjectAccessor() {
        return new DataAccessor.ReplySubject();
    }

    public DataAccessor<String> createSendSubjectAccessor() {
        return new DataAccessor.SendSubject();
    }

    public DataAccessor<String> createTrackingIdAccessor() {
        return new DataAccessor.TrackingId();
    }

    public DataAccessor<?> createFromDisplayName(String name) {
        return createFromString(namesToAccessorsMap, name, ERROR_BAD_NAME);
    }

    public DataAccessor<?> createFromIdentifier(String identifier) {
        return createFromString(identifiersToAccessorsMap, identifier, ERROR_BAD_IDENTIFIER);
    }

    private DataAccessor<?> createFromString(Map<String, Class<? extends DataAccessor<?>>> map, String string, String errorMessage) {
        Class<? extends DataAccessor<?>> clazz = map.get(checkNotNull(string));
        if (clazz == null) {
            throw new IllegalArgumentException(
                    MessageFormat.format(errorMessage, string));
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getDisplayNames() {
        final Set<String> names = namesToAccessorsMap.keySet();
        return names.toArray(new String[names.size()]);
    }

}
