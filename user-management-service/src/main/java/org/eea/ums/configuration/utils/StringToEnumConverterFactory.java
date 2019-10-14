package org.eea.ums.configuration.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The type String to enum converter factory.
 */
@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

  private static final Logger LOGGER_ERROR = LoggerFactory.getLogger("error_logger");

  private static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

    private Class<T> enumType;

    /**
     * Instantiates a new String to enum converter.
     *
     * @param enumType the enum type
     */
    StringToEnumConverter(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T convert(String source) {
      Method getEnumFromValueMethod = getCreatorMethod(this.enumType);
      T enumValue = null;
      if (null != getEnumFromValueMethod) {
        try {
          enumValue = (T) getEnumFromValueMethod.invoke(null, source);
        } catch (IllegalAccessException | InvocationTargetException e) {
          LOGGER_ERROR.error(
              "Error trying to invoke Method {} to build the Enum of type {}, using default Enum.valueOf method",
              getEnumFromValueMethod.getName(), this.enumType.getName());
        }
      }
      return enumValue == null ? (T) Enum.valueOf(this.enumType, source) : enumValue;
    }

    private Method getCreatorMethod(Class<T> type) {
      Method[] methods = type.getDeclaredMethods();
      Method result = null;
      for (Method method : methods) {
        if (method.isAnnotationPresent(JsonCreator.class)) {
          result = method;
        }
      }
      return result;
    }
  }

  @Override
  public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
    return new StringToEnumConverter(targetType);
  }


}
