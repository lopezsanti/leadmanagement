package com.pm.har.smartsheet;

import com.pm.har.model.LeadDto;
import com.smartsheet.api.SmartsheetException;

public interface SmartSheetService {

    void saveLead(LeadDto leadDto) throws SmartsheetException;

}
