package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;

import org.eea.interfaces.vo.dataset.DataSetVO;

public interface IFileParseContext {

	public DataSetVO parse(InputStream inputStream);
}
