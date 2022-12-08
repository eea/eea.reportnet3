package org.eea.interfaces.vo.recordstore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SplitSnapfile {

    /** The number of splitted files. */
    private int numberOfFiles;

    /** If the file is for splitting. */
    private boolean isForSplitting;
}
