package com.enicarthage.service;

import com.enicarthage.dao.DocteurDAO;
import com.enicarthage.model.DocteurInfo;

import java.sql.SQLException;
import java.util.List;

public class DocteurService {

    private final DocteurDAO docteurDAO;

    public DocteurService() {
        this.docteurDAO = new DocteurDAO();
    }

    public DocteurService(DocteurDAO docteurDAO) {
        this.docteurDAO = docteurDAO;
    }

    public List<DocteurInfo> getAllDoctors() throws SQLException {
        return docteurDAO.findAll();
    }
}
