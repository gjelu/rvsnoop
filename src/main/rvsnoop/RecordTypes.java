// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * RvSnoop allows the user to classify records based on fairly arbitrary criteria.
 * <p>
 * The mechanism used for this is the {@link RecordType} class, this object
 * holds a list of all the known types.
 */
public final class RecordTypes {

    // TODO make private again
    public class MessageTypeMatcherEditor extends AbstractMatcherEditor {
        MessageTypeMatcherEditor() {
            super();
        }
        public void constrain() {
            fireConstrained(getMatcher());
        }
        @Override
        public Matcher getMatcher() {
            return new Matcher() {
                public boolean matches(Object item) {
                    final Record record = (Record) item;
                    types.getReadWriteLock().readLock().lock();
                    try {
                        for (RecordType type : types) {
                            if (type.matches(record))
                                return type.isSelected();
                        }
                        return true;
                    } finally {
                        types.getReadWriteLock().readLock().unlock();
                    }
                }
            };
        }
        public void relax() {
            fireRelaxed(getMatcher());
        }
    }

    private static RecordTypes instance;

    public static final RecordType DEFAULT = new RecordType("Normal", Color.BLACK, RecordMatcher.DEFAULT_MATCHER);
    public static final RecordType ERROR = new RecordType("Error", Color.RED, new RecordMatcher.SendSubjectContains("ERROR"));

    public static synchronized RecordTypes getInstance() {
        if (instance == null) instance = new RecordTypes();
        return instance;
    }

    final MessageTypeMatcherEditor matcherEditor = new MessageTypeMatcherEditor();

    final EventList<RecordType> types = new BasicEventList<RecordType>();

    private RecordTypes() {
        reset();
    }

    public void clear() {
        types.getReadWriteLock().writeLock().lock();
        try {
            types.clear();
            types.add(DEFAULT);
        } finally {
            types.getReadWriteLock().writeLock().unlock();
        }
    }

    public RecordType createType() {
        return createType(generateName(), Color.BLACK, RecordMatcher.DEFAULT_MATCHER);
    }

    public RecordType createType(String name, Color color, RecordMatcher matcher) {
        if (RecordTypes.getInstance().isNameInUse(name))
            throw new IllegalArgumentException("Type name already in use: " + name);
        final RecordType type =  new RecordType(name, color, matcher);
        types.add(type);
        return type;
    }

    private String generateName() {
        types.getReadWriteLock().readLock().lock();
        try {
            final String[] nameArray = new String[types.size()];
            for (int i = types.size() - 1; i >= 0; --i) {
                nameArray[i] = types.get(i).getName();
            }
            final List names = Arrays.asList(nameArray);
            String name;
            for (int i = 1; true; ++i) {
                name = "My Record Type #" + Integer.toString(i);
                if (!names.contains(name)) return name;
            }
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public RecordType[] getAllTypes() {
        types.getReadWriteLock().readLock().lock();
        try {
            return types.toArray(new RecordType[types.size()]);
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public EventList getEventList() {
        return types;
    }

    public RecordType getFirstMatchingType(Record record) {
        types.getReadWriteLock().readLock().lock();
        try {
            for (RecordType type : types) {
                if (type.matches(record))
                    return type;
            }
            return DEFAULT;
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public MessageTypeMatcherEditor getMatcherEditor() {
        return matcherEditor;
    }

    public RecordType getType(int index) {
        return types.get(index);
    }

    public boolean isNameInUse(String name) {
        if (name == null) throw new NullPointerException();
        types.getReadWriteLock().readLock().lock();
        try {
            for (int i = 0, imax = types.size(); i < imax; ++i)
                if (name.equals(types.get(i).getName()))
                    return true;
            return false;
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public void removeType(RecordType type) {
        if (!DEFAULT.equals(type)) {
            types.getReadWriteLock().writeLock().lock();
            try {
                types.remove(type);
            } finally {
                types.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    public void reset() {
        types.getReadWriteLock().writeLock().lock();
        try {
            types.clear();
            types.add(ERROR);
            types.add(new RecordType("Warning", Color.ORANGE, new RecordMatcher.SendSubjectContains("WARN")));
            types.add(new RecordType("System", Color.GRAY, new RecordMatcher.SendSubjectStartsWith("_")));
            types.add(DEFAULT);
        } finally {
            types.getReadWriteLock().writeLock().unlock();
        }
    }

    public int size() {
        return types.size();
    }

}
