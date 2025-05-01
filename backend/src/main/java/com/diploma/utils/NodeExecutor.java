package com.diploma.utils;

import java.util.List;
import java.util.Map;

public interface NodeExecutor {
    Object execute(Map<String, Object> fields, List<String> inputs) throws Exception;
}