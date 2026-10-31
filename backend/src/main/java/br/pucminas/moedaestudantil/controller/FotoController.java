package br.pucminas.moedaestudantil.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.moedaestudantil.service.FotoStorage;

@RestController
public class FotoController {

    private final FotoStorage fotoStorage;

    public FotoController(FotoStorage fotoStorage) {
        this.fotoStorage = fotoStorage;
    }

    @GetMapping("/api/fotos/{arquivo}")
    public ResponseEntity<Resource> foto(@PathVariable String arquivo) {
        Resource foto = fotoStorage.carregar(arquivo);
        if (foto == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = MediaTypeFactory.getMediaType(arquivo).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(tipo).body(foto);
    }
}
