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
     * @return An array of integers that holds all database statistics.
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

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolMaximumConnections()
     */
    @Override
    public int getDatabasePoolMaximumConnections() {
        return getDBResults()[1];
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolMinimumConnections()
     */
    @Override
    public int getDatabasePoolMinimumConnections() {
        return getDBResults()[0];
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolOpenConnections()
     */
    @Override
    public int getDatabasePoolOpenConnections() {
        return getDBResults()[2];
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getDatabasePoolUsedConnections()
     */
    @Override
    public int getDatabasePoolUsedConnections() {
        return getDBResults()[3];
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaMsgRead()
     */
    @Override
    public long getMinaMsgRead() {
        return minaStatCollector.getMsgRead();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaMsgWritten()
     */
    @Override
    public long getMinaMsgWritten() {
        return minaStatCollector.getMsgWritten();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaQueuedEvents()
     */
    @Override
    public long getMinaQueuedEvents() {
        return minaStatCollector.getQueuedEvents();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getMinaScheduledWrites()
     */
    @Override
    public long getMinaScheduledWrites() {
        return minaStatCollector.getScheduledWrites();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolActiveThreadsCount()
     */
    @Override
    public int getThreadPoolActiveThreadsCount() {
        return getExecutor().getActiveCount();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolCompletedTaskCount()
     */
    @Override
    public long getThreadPoolCompletedTaskCount() {
        return getExecutor().getCompletedTaskCount();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolCoreSize()
     */
    @Override
    public int getThreadPoolCoreSize() {
        return getExecutor().getCorePoolSize();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getThreadPoolNumberOfQueuedTasks()
     */
    @Override
    public int getThreadPoolNumberOfQueuedTasks() {
        return getExecutor().getQueue().size();
    }

    /**
     * @see org.kjkoster.zapcat.example.openfire.ServerStatsMBean#getUserSessionsCount()
     */
    @Override
    public int getUserSessionsCount() {
        return SessionManager.getInstance().getUserSessionsCount(false);
    }
}
