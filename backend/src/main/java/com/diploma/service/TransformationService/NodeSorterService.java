package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.NodeSorterRequest;
import com.diploma.exception.NodeExecutionException;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import com.diploma.model.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@NodeType("node_sorter")
public class NodeSorterService implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(NodeSorterService.class);
    private final ResultService resultService;

    public NodeSorterService(ResultService resultService) {
        this.resultService = resultService;
    }

    @Override
    public Object execute(Node node) throws Exception {
        if (node.getInputs().isEmpty()) {
            throw new NodeExecutionException("❌ Sorter: Missing input node.");
        }

        try {
            UUID inputNodeId = node.getInputs().get(0).getNodeId();

            List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

            if (data == null) {
                throw new NodeExecutionException("❌ Sorter: Failed to get the result of the previous node.");
            }

            NodeSorterRequest request = new NodeSorterRequest();
            request.setData(data);
            request.setColumns((List<String>) node.getFields().get("columns")); 
            request.setAscending((boolean) node.getFields().getOrDefault("ascending", true));
            List<Map<String, Object>> sortedData = sortData(request);

            return Map.of("sortedData", sortedData);

        } catch (NodeExecutionException e) {
            throw e;

        } catch (Exception e) {
            log.error("Sorter execution failed in method execute()", e);
            throw new NodeExecutionException("❌ Sorter execution failed.");
        }
    }

    public List<Map<String, Object>> sortData(NodeSorterRequest request) throws Exception {
        try {
            if (request.getColumns() == null) {
                throw new NodeExecutionException("❌ Sorter: Required fields are null.");
            }

            List<Map<String, Object>> data = request.getData();

            if (data.isEmpty()) return data;

            // Используем Comparator для сортировки, без лишних преобразований
            Comparator<Map<String, Object>> comparator = (row1, row2) -> {
                for (String column : request.getColumns()) {
                    Comparable value1 = (Comparable) row1.get(column);
                    Comparable value2 = (Comparable) row2.get(column);

                    // Сравниваем значения колонок
                    int comparison = value1.compareTo(value2);
                    if (comparison != 0) {
                        return comparison; // Если значения разные, возвращаем результат сравнения
                    }
                }
                return 0; // Все значения одинаковые, строки равны
            };

            // Сортируем данные в зависимости от флага ascending
            if (request.isAscending()) {
                data.sort(comparator);
            } else {
                data.sort(comparator.reversed());
            }

            return data;

        } catch (NodeExecutionException e) {
            throw e; 

        } catch (ClassCastException e) {
            throw new NodeExecutionException("❌ Sorter: Invalid data type during sorting.");

        } catch (Exception e) {
            log.error("Sorter execution failed", e);
            throw new NodeExecutionException("❌ Sorter: Unknown error.");
        }
    }
}