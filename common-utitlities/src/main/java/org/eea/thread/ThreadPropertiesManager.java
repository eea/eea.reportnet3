package org.eea.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ThreadPropertiesManager.
 */
public class ThreadPropertiesManager {


  /** The thread. */
  protected static ThreadLocal<Map<String, Object>> thread = new InheritableThreadLocal<>();

  /**
   * Sets the variable.
   *
   * @param name the name
   * @param value the value
   */
  public static void setVariable(String name, Object value) {
    if (null == thread.get()) {
      thread.set(new HashMap<String, Object>());
    }
    thread.get().put(name, value);
  }

  /**
   * Gets the variable.
   *
   * @param name the name
   * @return the variable
   */
  public static Object getVariable(String name) {
    return thread.get().get(name);
  }
}
