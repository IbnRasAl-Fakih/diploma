package com.diploma.service.TransformationService;

import com.diploma.dto.TransformationDto.NodeSorterRequest;
import com.diploma.service.ResultService;
import com.diploma.utils.NodeExecutor;
import com.diploma.utils.NodeType;
import org.springframework.stereotype.Service;
import com.diploma.model.Node;
import java.util.*;


@Service
@NodeType("node_sorter")
public class NodeSorterService implements NodeExecutor {

    private final ResultService resultService;

    public NodeSorterService(ResultService resultService) {
        this.resultService = resultService;
    }


    @Override
    public Object execute(Node node) {
        if (node.getInputs().isEmpty()) {
            throw new IllegalArgumentException("NodeSorter требует хотя бы один input (nodeId)");
        }

        UUID inputNodeId = node.getInputs().get(0).getNodeId();

        List<Map<String, Object>> data = resultService.getDataFromNode(inputNodeId);

        NodeSorterRequest request = new NodeSorterRequest();
        request.setData(data);
        request.setColumns((List<String>) node.getFields().get("columns")); 
        request.setAscending((boolean) node.getFields().getOrDefault("ascending", true));
        List<Map<String, Object>> sortedData = sortData(request);

        return Map.of("sortedData", sortedData);
    }


    public List<Map<String, Object>> sortData(NodeSorterRequest request) {
        List<Map<String, Object>> data = request.getData();

        // Проверка на пустые данные
        if (data == null || data.isEmpty() || request.getColumns().isEmpty()) {
            return data;
        }

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
    }

}
