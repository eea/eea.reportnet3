package org.eea.enums.utils;

import org.eea.exception.EEAErrorMessage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The type String to enum converter factory.
 */
public class StringToLongConverterFactory implements ConverterFactory<String, Long> {


  /**
   * The Class StringToEnumConverter.
   *
   * @param <T> the generic type
   */
  private static class StringToLongConverter<T extends Long> implements Converter<String, T> {

    private Class<T> longValue;

    /**
     * Instantiates a new String to enum converter.
     *
     * @param enumType the enum type
     */
    StringToLongConverter(Class<T> longValue) {
      this.longValue = longValue;
    }

    @Override
    public T convert(String source) {
      T longValue = null;
      try {
        long longValueAux = Long.parseLong(source);
        longValue = (T) Long.valueOf(longValueAux);
      } catch (NumberFormatException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.CONVERTING_FROM_STRING_TO_LONG);
      }
      return longValue;
    }
  }

  /**
   * Gets the converter.
   *
   * @param <T> the generic type
   * @param targetType the target type
   *
   * @return the converter
   */
  @Override
  public <T extends Long> Converter<String, T> getConverter(Class<T> longValue) {
    return new StringToLongConverter(longValue);
  }
}
