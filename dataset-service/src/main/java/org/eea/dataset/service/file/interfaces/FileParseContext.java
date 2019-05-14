package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;

import org.eea.interfaces.vo.dataset.DataSetVO;

public interface FileParseContext {

	public DataSetVO parse(InputStream inputStream);
}
