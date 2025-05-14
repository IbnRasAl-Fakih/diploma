package com.diploma.model;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Node {

    private UUID nodeId;

    private String type;

    private List<Node> inputs;

    private Map<String, Object> fields;


    @Override
    public String toString() {
        StringBuilder inputsIds = new StringBuilder();
        if (inputs != null) {
            for (Node input : inputs) {
                if (input != null && input.getNodeId() != null) {
                    inputsIds.append(input.getNodeId().toString()).append(", ");
                }
            }
            if (inputsIds.length() > 2) {
                inputsIds.setLength(inputsIds.length() - 2); // Убрать последний ", "
            }
        }

        return "Node{" +
                "nodeId=" + nodeId +
                ", type='" + type + '\'' +
                ", inputs=[" + inputsIds.toString() + "]" +
                ", fields=" + fields +
                '}';
    }
}