package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Giao diện DAO chung
public interface IGenericDAO<T, ID> {

    // Thêm
    boolean insert(T entity) throws SQLException;

    // Cập nhật
    boolean update(T entity) throws SQLException;

    // Xóa
    boolean delete(ID id) throws SQLException;

    // Tìm theo ID
    Optional<T> findById(ID id) throws SQLException;

    // Lấy tất cả
    List<T> findAll() throws SQLException;

    // Đếm số lượng
    int count() throws SQLException;
}
