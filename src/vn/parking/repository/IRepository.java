package vn.parking.repository;

import java.util.List;

/**
 * Interface generic cho các repository
 */
public interface IRepository<T> {

    List<T> getAll();

    void add(T item);

    void remove(T item);
}
