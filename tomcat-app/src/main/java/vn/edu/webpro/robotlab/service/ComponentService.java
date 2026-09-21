package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import vn.edu.webpro.robotlab.dao.ComponentDao;
import vn.edu.webpro.robotlab.model.ComponentPage;

/** Applies pagination rules before delegating to the component DAO. */
public final class ComponentService {
    private final ComponentDao componentDao;

    public ComponentService(ComponentDao componentDao) {
        this.componentDao = componentDao;
    }

    public ComponentPage list(int page, int limit) throws SQLException {
        if (page < 1 || page > 100000 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Phân trang không hợp lệ.");
        }
        return componentDao.list(page, limit);
    }
}
