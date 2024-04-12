package org.eea.interfaces.vo.dataset;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDLVO {

    /** The Constant serialVersionUID */
    private static final long serialVersionUID = -5875161356251419768L;

    /** The fileName */
    private String fileName;

    /** The content */
    private byte[] content;
}
