package com.pm.har.smartsheet.impl;

import com.pm.har.model.LeadColumnName;
import com.pm.har.model.LeadDto;
import com.pm.har.smartsheet.SmartSheetService;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmartSheetServiceImpl implements SmartSheetService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Smartsheet smartsheet;

    @Value("${app.smartsheet.sheet.name}")
    private String sheetName;

    @Override
    public void saveLead(LeadDto leadDto) throws SmartsheetException {
        Long sheetId = getSheetId(sheetName);
        if (sheetId == null) {
            sheetId = createSheet(sheetName);
        }

        addRow(sheetId, leadDto);
        logger.debug("Lead {} added to sheet {}", leadDto, sheetName);
    }

    public void addRow(Long sheetId, LeadDto leadDto) throws SmartsheetException {
        Set<String> knownLeadColumns = Arrays.stream(LeadColumnName.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        List<Column> columnDefinitions = getSheetColumns(sheetId);

        Cell.AddRowCellsBuilder rowCellsBuilder = new Cell.AddRowCellsBuilder();

        columnDefinitions.stream()
                .filter(d -> knownLeadColumns.contains(d.getTitle()))
                .forEach(d -> rowCellsBuilder
                        .addCell(d.getId(),
                                Optional.ofNullable(
                                        leadDto.get(LeadColumnName.valueOf(d.getTitle()))
                                ).orElse("")
                        )
                );

        List<Cell> rowCells = rowCellsBuilder.build();
        Row row = new Row.AddRowBuilder().setCells(rowCells).setToBottom(true).build();
        smartsheet.sheetResources().rowResources()
                .addRows(sheetId, Collections.singletonList(row));

    }

    public List<Column> getSheetColumns(Long sheetId) throws SmartsheetException {
        PaginationParameters page = new PaginationParameters.PaginationParametersBuilder().setIncludeAll(true).build();
        PagedResult<Column> pagedResult = smartsheet.sheetResources().columnResources()
                .listColumns(sheetId, null, page);
        return pagedResult.getData();
    }

    public Long getSheetId(String sheetName) throws SmartsheetException {
        PaginationParameters page = new PaginationParameters.PaginationParametersBuilder().setIncludeAll(true).build();
        PagedResult<Sheet> sheetPagedResult;
        do {
            sheetPagedResult = smartsheet.sheetResources().listSheets(null, page);
            for (Sheet sheet : sheetPagedResult.getData()) {
                if (sheet.getName().equals(sheetName)) {
                    return sheet.getId();
                }
            }
        } while (!sheetPagedResult.getPageNumber().equals(sheetPagedResult.getTotalPages()));
        // sheet not found by name

        return null;
    }

    public Long createSheet(String sheetName) throws SmartsheetException {
        List<Column> columns = Arrays.stream(LeadColumnName.values())
                .map(n -> new Column.AddColumnToSheetBuilder()
                        .setTitle(n.name()).setPrimary(n.isPrimary())
                        .setType(ColumnType.TEXT_NUMBER)
                        .build())
                .collect(Collectors.toList());


        Sheet sheet = new Sheet.CreateSheetBuilder()
                .setName(sheetName)
                .setColumns(columns)
                .build();

        sheet = smartsheet.sheetResources()
                .createSheet(sheet);
        return sheet.getId();
    }


}
