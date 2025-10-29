package com.unicauca.fiet.sistema_electivas.archivo.repository;


import com.unicauca.fiet.sistema_electivas.archivo.model.CargaArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargaArchivoRepository extends JpaRepository<CargaArchivo, Long> {
}
