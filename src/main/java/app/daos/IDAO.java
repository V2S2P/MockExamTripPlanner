package app.daos;

import java.util.List;

public interface IDAO <T,I>{
    T create(T t);
    T getById(int id);
    List<T> getAll();
    T update(int id, T t);
    boolean deleteById(int id);
}
