package com.ne.backend.service;

import com.ne.backend.entity.ExampleEntity;
import com.ne.backend.repository.ExampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    public List<ExampleEntity> getAll() {
        return exampleRepository.findAll();
    }

    public ExampleEntity getById(Long id) {

        return exampleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Example not found"));
    }

    public ExampleEntity create(ExampleEntity entity) {
        return exampleRepository.save(entity);
    }

    public ExampleEntity update(Long id, ExampleEntity request) {

        ExampleEntity entity = getById(id);

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        return exampleRepository.save(entity);
    }

    public void delete(Long id) {

        ExampleEntity entity = getById(id);

        exampleRepository.delete(entity);
    }
}