package com.example.costestimator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.costestimator.data.Material;
import com.example.costestimator.repository.MaterialRepository;

import java.util.ArrayList;
import java.util.List;

@RestController // defines class as HTTP request and retuns method output as json
public class MaterialController {

  private final MaterialRepository materialRepository;

  public MaterialController(MaterialRepository materialRepository) {
    this.materialRepository = materialRepository;
  }

  @GetMapping("/materials") // when get request at materials return this
  public List<Material> listMaterials() {
    return materialRepository.findByActiveTrue();
  }

  @PostMapping("/materials")
  @ResponseStatus(HttpStatus.CREATED)
  public Material addMaterial(@RequestBody Material material) { // return type material from JSON made from material
                                                                // object
    return materialRepository.save(material);
  }
}
