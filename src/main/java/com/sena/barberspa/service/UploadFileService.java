package com.sena.barberspa.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileService {

	private String folder = "images//";

	// metodo para subir la imagen del producto
	public String saveImages(MultipartFile file, String nombreproducto) throws IOException {
		// validacion de imagenes
		if (!file.isEmpty()) {
			byte[] bytes = file.getBytes();
			// variable de tipo path que redirige al directo
			// se importa el path de .nio.file
			Path path = Paths.get(folder + nombreproducto + "_" + file.getOriginalFilename());
			Files.write(path, bytes);
			return nombreproducto + "_" + file.getOriginalFilename();
		}
		return "default.jpg";
	}
	
	//metodo para la eliminacion de la imagen del producto
	public void deleteImage(String nombreproducto) {
		String ruta = "//images";
		File file = new File(ruta + nombreproducto);
		file.delete();
	}
}
