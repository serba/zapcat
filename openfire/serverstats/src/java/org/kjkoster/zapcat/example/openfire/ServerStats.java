package org.kjkoster.zapcat.example.openfire;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.mina.common.ExecutorThreadModel;
import org.apache.mina.management.MINAStatCollector;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.util.JiveGlobals;

/**
 * Implementation of the ServerStats MBean. The implementation has been largely
 * borrowed from Jives unofficial loadStats plugin. This code makes use of some
 * evil hacks to get information from Openfire, but it suits our purpose of
 * showing how easy the functionality that the Zapcat plugin offers can be
 * extended.
 * 
 * @author Guus der Kinderen, guus@nimbuzz.com
 * 
 */
/**
 * @author Guus der Kinderen, guus@nimbuzz.com
 * 
 */
public class ServerStats implements ServerStatsMBean {
    private SocketAcceptor socketAcceptor;

    private final MINAStatCollector minaStatCollector;

    /**
     * Constructs several items that are used to gather statistics from.
     */
    public ServerStats() {
        final ConnectionManagerImpl connectionManager = ((ConnectionManagerImpl) XMPPServer
                .getInstance().getConnectionManager());
        if (JiveGlobals
                .getBooleanProperty("statistic.connectionmanager", false)) {
            socketAcceptor = connectionManager.getMultiplexerSocketAcceptor();
        } else {
            socketAcceptor = connectionManager.getSocketAcceptor();
        }
        minaStatCollector = new MINAStatCollector(socketAcceptor);
    }

    /**
     * Utility method to parse the database statistics through the toString()
     * hack.
     * 
     * @return Array of integer that holds all database statistics.
     */
    private static int[] getDBResults() {
        final String[] textresults = DbConnectionManager
                .getConnectionProvider().toString().split(",");
        final int[] results = new int[textresults.length];
        for (int i = 0; i < textresults.length; i++) {
            results[i] = Integer.parseInt(textresults[i]);
        }

        return results;
    }

    /**
     * Utility method for retrieving the ThreadPoolExecutor that represents the
     * core thread pool.
     * 
     * @return Core thread pool representation.
     */
    private ThreadPoolExecutor getExecutor() {
        final ExecutorThreadModel threadModel = (ExecutorThreadModel) socketAcceptor
                .getDefaultConfig().getThreadModel();
        return (ThreadPoolExecutor) threadModel.getExecutor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolMaximumConnections()
     */
    @Override
    public int getDatabasePoolMaximumConnections() {
        return getDBResults()[1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolMinimumConnections()
     */
    @Override
    public int getDatabasePoolMinimumConnections() {
        return getDBResults()[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolOpenConnections()
     */
    @Override
    public int getDatabasePoolOpenConnections() {
        return getDBResults()[2];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolUsedConnections()
     */
    @Override
    public int getDatabasePoolUsedConnections() {
        return getDBResults()[3];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaMsgRead()
     */
    @Override
    public long getMinaMsgRead() {
        return minaStatCollector.getMsgRead();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaMsgWritten()
     */
    @Override
    public long getMinaMsgWritten() {
        return minaStatCollector.getMsgWritten();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaQueuedEvents()
     */
    @Override
    public long getMinaQueuedEvents() {
        return minaStatCollector.getQueuedEvents();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaScheduledWrites()
     */
    @Override
    public long getMinaScheduledWrites() {
        return minaStatCollector.getScheduledWrites();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolActiveThreadsCount()
     */
    @Override
    public int getThreadPoolActiveThreadsCount() {
        return getExecutor().getActiveCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolCompletedTaskCount()
     */
    @Override
    public long getThreadPoolCompletedTaskCount() {
        return getExecutor().getCompletedTaskCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolCoreSize()
     */
    @Override
    public int getThreadPoolCoreSize() {
        return getExecutor().getCorePoolSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolNumberOfQueuedTasks()
     */
    @Override
    public int getThreadPoolNumberOfQueuedTasks() {
        return getExecutor().getQueue().size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getUserSessionsCount()
     */
    @Override
    public int getUserSessionsCount() {
        return SessionManager.getInstance().getUserSessionsCount(false);
    }
}
