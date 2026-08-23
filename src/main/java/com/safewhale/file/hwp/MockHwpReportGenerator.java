package com.safewhale.file.hwp;

import com.safewhale.report.domain.Report;
import org.springframework.stereotype.Component;

@Component
public class MockHwpReportGenerator implements HwpReportGenerator {
    @Override
    public String generate(Report report) {
        return "/files/mock-report-" + report.getId() + ".hwp";
    }
}
