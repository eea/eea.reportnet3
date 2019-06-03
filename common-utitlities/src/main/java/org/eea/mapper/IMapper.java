package org.eea.mapper;

import java.util.List;

/**
 * The Interface IMapper.
 *
 * @param <X> the generic type
 * @param <T> the generic type
 */
public interface IMapper<X, T> {

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the x
   */
  X classToEntity(T model);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the t
   */
  T entityToClass(X entity);

  /**
   * Class list to entity.
   *
   * @param model the model
   * @return the list
   */
  List<X> classListToEntity(List<T> model);

  /**
   * Entity list to class.
   *
   * @param entity the entity
   * @return the list
   */
  List<T> entityListToClass(List<X> entity);
}
