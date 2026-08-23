package com.safewhale.file.hwp;

import com.safewhale.report.domain.Report;

public interface HwpReportGenerator {
    String generate(Report report);
}
