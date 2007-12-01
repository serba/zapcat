package org.kjkoster.zapcat.openfire;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.jivesoftware.util.Log;

/**
 * A Log4J appender that sends its log entries to the Openfire logs.
 * 
 * @author Guus der Kinderen, guus@nimbuzz.com
 */
public class OpenfireLog4jAppender extends AppenderSkeleton {

    /**
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    // used to be backwards compatible. Ignore deprecation warnings.
    @SuppressWarnings("deprecation")
    @Override
    protected void append(final LoggingEvent event) {
        final String message = event.getMessage().toString();

        Throwable throwable = null;
        if (event.getThrowableInformation() != null) {
            throwable = event.getThrowableInformation().getThrowable();
        }

        switch (event.getLevel().toInt()) {
        case Priority.OFF_INT:
            // Logging turned off - do nothing.
            break;

        case Priority.FATAL_INT:
        case Priority.ERROR_INT:
            Log.error(message, throwable);
            break;

        case Priority.WARN_INT:
            Log.warn(message, throwable);
            break;

        case Priority.INFO_INT:
            Log.info(message, throwable);
            break;

        default:
            // DEBUG and below (trace, all)
            Log.debug(message, throwable);
            break;
        }
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    public void close() {
        // There's nothing here to close.
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    public boolean requiresLayout() {
        // we're doing this quick and dirty.
        return false;
    }
}