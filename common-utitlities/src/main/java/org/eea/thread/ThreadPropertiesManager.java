package org.eea.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ThreadPropertiesManager.
 */
public class ThreadPropertiesManager {


  /** The thread. */
  protected static ThreadLocal<Map<String, Object>> thread = new InheritableThreadLocal<>();

  protected static ThreadLocal<String> user = new InheritableThreadLocal<>();

  /**
   * Sets the variable.
   *
   * @param name the name
   * @param value the value
   */
  public static void setVariable(String name, Object value) {
    if (name != null && name.equals("user")) {
      user.set(((String) value).intern());
    } else {
      if (null == thread.get()) {
        thread.set(new HashMap<String, Object>());
      }
      thread.get().put(name, value);
    }
  }

  /**
   * Gets the variable.
   *
   * @param name the name
   * @return the variable
   */
  public static Object getVariable(String name) {
    if (name.equals("user")) {
      return user.get();
    }
    return thread.get().get(name);
  }
}
