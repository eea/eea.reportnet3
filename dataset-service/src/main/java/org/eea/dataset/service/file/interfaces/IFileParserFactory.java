package org.eea.dataset.service.file.interfaces;

public interface IFileParserFactory {
	public IFileParseContext createContext(String mimeType);
}
