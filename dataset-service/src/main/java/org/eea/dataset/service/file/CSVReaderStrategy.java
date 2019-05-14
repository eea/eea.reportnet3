package org.eea.dataset.service.file;

import java.io.InputStream;

import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

public class CSVReaderStrategy implements ReaderStrategy {

    @Override
    public DataSetVO parseFile(InputStream inputStream) {
    	return null;
    }

}
