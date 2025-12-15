package org.eea.interfaces.vo.dataset.schemas;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DatasetEditingStatusVO.
 */
@Getter
@Setter
@ToString
public class DatasetEditingStatusVO {

    /** The dataset id. */
    private Long datasetId;

    /** Whether the dataset is currently being edited. */
    private Boolean isEditing;

    /** The username of the editor (null if not being edited). */
    private String editor;
    /** Whether the dataset is locked for user */
    private Boolean isLockedForUser;

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DatasetEditingStatusVO other = (DatasetEditingStatusVO) obj;
        return Objects.equals(datasetId, other.datasetId)
                && Objects.equals(isEditing, other.isEditing)
                && Objects.equals(editor, other.editor);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(datasetId, isEditing, editor);
    }
}
