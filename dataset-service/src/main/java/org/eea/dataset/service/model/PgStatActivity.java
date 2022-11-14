package org.eea.dataset.service.model;

import lombok.Getter;
import lombok.Setter;
import org.postgresql.util.PGInterval;

@Getter
@Setter
public class PgStatActivity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String pid;
    private String userName;
    private String applicationName;
    private String query;
}















