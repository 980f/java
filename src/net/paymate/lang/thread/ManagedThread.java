package net.paymate.lang.thread;
import net.paymate.util.Resetable;

/**
 * Interface for threads managed by ThreadManager. A class must implement this
 * interface in order to be started up by ThreadManager class.
 */
public interface ManagedThread extends Server, Resetable, Runnable {
  String getName();
}