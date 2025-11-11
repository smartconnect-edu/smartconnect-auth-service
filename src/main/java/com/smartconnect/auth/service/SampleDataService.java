package com.smartconnect.auth.service;

public interface SampleDataService {

    SampleDataSummary generateSampleData(int adminCount, int teacherCount, int studentCount);

    @lombok.Data
    @lombok.Builder
    class SampleDataSummary {
        private int usersGenerated;
        private int adminsGenerated;
        private int teachersGenerated;
        private int studentsGenerated;
    }
}

