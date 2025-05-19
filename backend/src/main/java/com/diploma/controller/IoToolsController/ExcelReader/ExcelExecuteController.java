package com.diploma.controller.IoToolsController.ExcelReader;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.diploma.dto.IoToolsDto.ExcelReader.*;

import com.diploma.dto.ResultProcessorDto;
import com.diploma.service.IoToolsService.ExcelReadService;
import com.diploma.utils.ResultProcessor;

@RestController
@RequestMapping("/api/excel")
public class ExcelExecuteController {
      @Autowired
      private ResultProcessor processor;
    
@PostMapping(value = "/execute-node", consumes = { "multipart/form-data" })
public ResponseEntity<?> executeExcelReaderNode(
        @RequestPart("file") MultipartFile file,
        @RequestPart("params") ExcelExecutionRequest request
) {
    try {
        ExcelReaderRequest params = request.getReaderParams();
        List<Map<String, Object>> result = ExcelReadService.readExcel(file, params);
        
        processor.putToDatabase(new ResultProcessorDto(
                request.getNodeId(),
                request.getType(),
                request.getWorkflowId(),
                result
        ));

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "rowCount", result.size(),
                "nodeId", request.getNodeId()
        ));

    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
        ));
    }
}

}
