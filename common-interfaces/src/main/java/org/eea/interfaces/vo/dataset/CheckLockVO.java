package org.eea.interfaces.vo.dataset;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CheckLockVO implements Serializable {

    /** The Constant serialVersionUID */
    private static final long serialVersionUID = -5875161356251419768L;

    /** The description */
    private String message;

    /** The importInProgress */
    private boolean importInProgress;
}
