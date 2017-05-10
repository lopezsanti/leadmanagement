package com.pm.har.smartsheet;

import com.pm.har.model.LeadColumnName;
import com.pm.har.smartsheet.impl.SmartSheetServiceImpl;
import com.smartsheet.api.models.Column;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"app.smartsheet.sheet.name=${random.uuid}"})
public class SmartSheetServiceIT {

    private String sheetName;

    @Autowired
    private SmartSheetServiceImpl smartSheetService;

    @Before
    public void setUp() throws Exception {
        sheetName = "Test_" + UUID.randomUUID().toString();

    }

    @Test
    public void createSheet() throws Exception {
        Long sheetId = smartSheetService.createSheet(sheetName);
        assertNotNull(sheetId);
        Long sameId = smartSheetService.getSheetId(sheetName);
        assertEquals(sheetId, sameId);
        List<Column> sheetColumns = smartSheetService.getSheetColumns(sheetId);
        assertEquals(LeadColumnName.values().length, sheetColumns.size());
        // column titles can be resolved to constants
        sheetColumns.forEach(c -> LeadColumnName.valueOf(c.getTitle()));
    }
}
