package org.eea.dataset.service.file;

import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.springframework.stereotype.Component;

@Component
public class FileParserFactory implements IFileParserFactory {
	@Override
	public IFileParseContext createContext(String mimeType) {
		FileParseContextImpl context = null;
		mimeType = mimeType.toLowerCase();
		switch (mimeType) {
		case "csv":
			context = new FileParseContextImpl(new CSVReaderStrategy());
			break;
		case "xml":
			context = new FileParseContextImpl(new XMLReaderStrategy());
			break;
		default:
			context = null;
			break;
		}
		return context;
	}
}
