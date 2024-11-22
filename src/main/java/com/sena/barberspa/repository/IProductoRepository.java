package com.sena.barberspa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sena.barberspa.model.Producto;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Integer> {

}
