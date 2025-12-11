package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ImportedFilesDirectoriesVO.
 */
@Getter
@Setter
@ToString
public class ImportedFilesDirectoriesVO implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5291743298571300213L;

    /** The file name. */
    private String fileName;

    /** The file size (in bytes or human-readable format). */
    private String fileSize;

    /** The file creation date (import date in ISO format). */
    private String creationDate;

    /** The file creation timestamp (raw) */
    private long creationDateTimestamp;

    /** The file last modified time (raw) */
    private long lastModifiedTime;

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileSize, creationDate, creationDateTimestamp, lastModifiedTime);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ImportedFilesDirectoriesVO other = (ImportedFilesDirectoriesVO) obj;
        return Objects.equals(fileName, other.fileName)
                && Objects.equals(fileSize, other.fileSize)
                && Objects.equals(creationDate, other.creationDate)
                && creationDateTimestamp == other.creationDateTimestamp
                && lastModifiedTime == other.lastModifiedTime;
    }
}
