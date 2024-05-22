package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobPresignedUrlInfo implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1348263779137653665L;

    /** The job Id. */
    private Long jobId;

    /** The presigned url */
    private String presignedUrl;

    /** The file path in s3 */
    private String filePathInS3;
}
