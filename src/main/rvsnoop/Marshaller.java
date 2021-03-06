// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

import com.tibco.sdk.MTree;
import com.tibco.tibrv.TibrvMsg;
import org.rvsnoop.Logger;

/**
 * Utility methods to convert Rendezvous messages to specific string
 * representations and back again.
 * <p>
 * This class will select the "best" conversion method on loading. Not all
 * conversion methods work both ways, but all can convert messages to strings.
 */
public final class Marshaller {

    public abstract static class Implementation {
        private final String name;
        Implementation(String name) {
            this.name = name;
        }
        public final String getName() {
            return name;
        }
        public String marshal(String name, TibrvMsg message) {
            throw new UnsupportedOperationException();
        }
        public TibrvMsg unmarshal(String string) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Marshaller implementation that uses SDK MTree's to do the work.
     */
    static final class MTreeImpl extends Marshaller.Implementation {

        MTreeImpl() {
            super("MTree");
            try {
                Class.forName("com.tibco.sdk.MTree");
            } catch (ClassNotFoundException e) {
                // Handled by Marshaller static initializer.
            }
        }

        @Override
        public String marshal(String name, TibrvMsg message) {
            final MTree mtree = new MTree(name);
            mtree.use_tibrvMsg(message);
            return mtree.toString();

        }

    }

    /**
     * Marshaller implementation that just calls <code>toString()</code> to do the work.
     * <p>
     * This implementation does not support unmarshalling strings to messages.
     */
    static final class RvMsgImpl extends Marshaller.Implementation {

        RvMsgImpl() {
            super("TibrvMsg");
        }

        @Override
        public String marshal(String name, TibrvMsg message) {
            return message.toString();
        }

    }

    public static final String IMPL_MTREE = "rvsnoop.Marshaller$MTreeImpl";
    public static final String IMPL_RVMSG = "rvsnoop.Marshaller$RvMsgImpl";

    private static final Implementation implementation;

    private static final Logger logger = Logger.getLogger();

    private static final String[] PREFERRED = { IMPL_MTREE, IMPL_RVMSG };

    public static Implementation getImplementation(String className) {
        try {
            return (Implementation) Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.debug("Failed to load marshaller: %s", className);
            return null;
        } catch (NoClassDefFoundError e) {
            logger.debug("Failed to load marshaller: %s", className);
            return null;
        }
    }

    static {
        // Allow custom marshallers via a system preference.
        String additional = System.getProperty("rvsnoop.marshaller");
        String[] preferred = new String[additional == null ? 4 : 5];
        if (additional != null) preferred[0] = additional;
        System.arraycopy(PREFERRED, 0, preferred, additional == null ? 0 : 1, 4);
        Implementation impl = null;
        for (String s : preferred)
            if ((impl = getImplementation(s)) != null)
                break;
        implementation = impl;
        if (impl == null) {
            logger.info("No marshaller loaded!");
        } else {
            logger.info("Using marshaller: %s", impl.name);
        }
    }

    /**
     * Get the name of the marshaller implementation that is in use.
     *
     * @return The name of the marshaller.
     */
    public static String getImplementationName() {
        if (implementation == null) {
            throw new UnsupportedOperationException();
        }
        return implementation.getName();
    }

    /**
     * Marshal a given message to it's string representation.
     *
     * @param name The "name" of the message. Some marshallers will use this as
     *        part of the output while others will ignore it.
     * @param message The message to marshal.
     * @return The message's string form
     */
    public static String marshal(String name, TibrvMsg message) {
        if (implementation == null) {
            throw new UnsupportedOperationException();
        }
        return implementation.marshal(name, message);
    }

    /**
     * Unmarshal a message from it's string form.
     * <p>
     * In general, only the more advanced marshallers can perform this operation.
     *
     * @param string The string form of the message.
     * @return The message.
     * @throws UnsupportedOperationException
     */
    public static TibrvMsg unmarshal(String string) {
        if (implementation == null) {
            throw new UnsupportedOperationException();
        }
        return implementation.unmarshal(string);
    }

    /**
     * Do not instantiate.
     */
    private Marshaller() {
        throw new UnsupportedOperationException();
    }

}
