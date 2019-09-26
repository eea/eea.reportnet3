package org.eea.communication.service.model;

public class Notification {
  /**
   * The Class Notification.
   */

    /** The content. */
    private String content;

    /**
     * Instantiates a new notification.
     *
     * @param message the message
     */
    public Notification(String message) {
      this.content = message;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public String getContent() {
      return content;
    }
  }
}
