package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webpro.robotlab.dao.LibraryResourceDao;
import vn.edu.webpro.robotlab.model.LibraryResource;

/** Kiểm tra phân trang trước khi giao cho DAO đọc thư viện tài nguyên. */
public final class LibraryResourceService {
    private static final int MAX_PAGE = 100000;
    private static final int MAX_LIMIT = 100;

    private final LibraryResourceDao resources;

    public LibraryResourceService(LibraryResourceDao resources) {
        this.resources = resources;
    }

    public List<LibraryResource> list(int page, int limit, String robotId) throws SQLException {
        // Chặn limit quá lớn để một request không kéo cả bảng ra khỏi database.
        if (page < 1 || page > MAX_PAGE || limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("pagination");
        }
        return resources.list(limit, (page - 1) * limit, robotId);
    }

    public long count(String robotId) throws SQLException {
        return resources.count(robotId);
    }
}
