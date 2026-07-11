package com.bmitracker.service;

import com.bmitracker.model.BmiRecord;
import java.util.List;

public interface BmiRecordService {
    List<BmiRecord> getRecordsByUserId(int userId);
}
