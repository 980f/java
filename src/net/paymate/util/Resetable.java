package net.paymate.util;

/**
 * Any object that one can "reset" implements this. The meaning of "reset"
 * is rather ill-defined, but usually means to bring itself back to a starting
 * state, to clear its caches and/or to re-read configuration files.
 */
public interface Resetable {
  public void reset() throws Exception;
}
