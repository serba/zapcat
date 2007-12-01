package org.kjkoster.zapcat.example.openfire;

/**
 * An MBean definition based on the unofficial 'loadStats' plugin.
 * 
 * @author Guus der Kinderen, guus@nimbuzz.com
 */
public interface ServerStatsMBean {

    /**
     * The Openfire database pool can be configured to keep a minimum number of
     * connections to the database open, even if there is no database activity.
     * This method will return this minimum amount of connections.
     * 
     * @return The minimum number of database connections that the pool will
     *         always keep connected.
     */
    public int getDatabasePoolMinimumConnections();

    /**
     * The Openfire database pool has an upper limit of concurrent open database
     * connections. This method returns that limit.
     * 
     * @return The maximum number of concurrent database connections.
     */
    public int getDatabasePoolMaximumConnections();

    /**
     * The Openfire database pool opens and closes concurrent connections based
     * on demand. This method returns the number of database connections that
     * are currently open.
     * 
     * @return The number of database connections that are opened.
     */
    public int getDatabasePoolOpenConnections();

    /**
     * A subset of the database connections that are opened by the database pool
     * are in use by Openfire components. This method will return the amount of
     * those connections.
     * 
     * @return The number of database connections that are opened and in use.
     */
    public int getDatabasePoolUsedConnections();

    /**
     * Returns the number of threads that in the core thread pool of Openfire.
     * This pool is a fixed size pool.
     * 
     * @return The number of threads the core thread pool.
     */
    public int getThreadPoolCoreSize();

    /**
     * The number of threads in the core thread pool that are currently active.
     * 
     * @return The number of active core threads.
     */
    public int getThreadPoolActiveThreadsCount();

    /**
     * The number of tasks that's currently queued for execution by a thread
     * from the core thread pool.
     * 
     * @return The core thread pool queue size.
     */
    public int getThreadPoolNumberOfQueuedTasks();

    /**
     * The number of tasks that have been run by the threads in the core thread
     * pool.
     * 
     * @return Number of tasks completed by the core thread pool.
     */
    public long getThreadPoolCompletedTaskCount();

    /**
     * The number of user sessions that are currently running on this server
     * node.
     * 
     * @return Amount of user sessions.
     */
    public int getUserSessionsCount();

    /**
     * @see org.apache.mina.management.MINAStatCollector#getMsgRead()
     */
    public long getMinaMsgRead();

    /**
     * @see org.apache.mina.management.MINAStatCollector#getMsgWritten()
     */
    public long getMinaMsgWritten();

    /**
     * @see org.apache.mina.management.MINAStatCollector#getQueuedEvents()
     */
    public long getMinaQueuedEvents();

    /**
     * @see org.apache.mina.management.MINAStatCollector#getScheduledWrites()
     */
    public long getMinaScheduledWrites();
}
