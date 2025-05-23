package com.diploma.service;

import com.diploma.model.User;
import com.diploma.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
