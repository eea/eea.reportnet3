package org.eea.dataset.service.file;

import java.io.InputStream;

import org.eea.dataset.service.file.interfaces.FileParseContext;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

public class FileParseContextImpl implements FileParseContext {

	private ReaderStrategy readerStrategy;

	public FileParseContextImpl(ReaderStrategy readerStrategy) {
		this.readerStrategy = readerStrategy;
	}

	public DataSetVO parse(InputStream inputStream) {
		return readerStrategy.parseFile(inputStream);
	}
}
