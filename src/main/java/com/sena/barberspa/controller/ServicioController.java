package com.sena.barberspa.controller;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sena.barberspa.model.Servicio;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IServiciosService;
import com.sena.barberspa.service.IUsuarioService;
import com.sena.barberspa.service.UploadFileService;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

	// Instancia de LOGGER para ver datos en consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(ServicioController.class);

	@Autowired
	private IServiciosService servicioService;

	@Autowired
	private IUsuarioService usuarioService;

	// microsevicio imagenes

	@Autowired
	private UploadFileService upload;

	// metodo para redirigir a la vista show en el template de productos
	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("servicios", servicioService.findAll());
		return "servicios/show";
	}

	// metodo es el que redirige a la vista de creacion de productos
	@GetMapping("/create")
	public String create() {                                                                                                                                                                                                                                                                                                                                                         
		return "servicios/create";
	}

	// metodo de creacion de productos
	@PostMapping("/save")
	public String save(Servicio servicio, @RequestParam("img") MultipartFile file) throws IOException {
		LOGGER.info("Este es el objeto del servicio a guardar en la DB {}", servicio);
		Usuario u = new Usuario(1, "", "", "", "", "", "", "");
		servicio.setUsuario(u);
		// validacion imagen del producto

		if (servicio.getId() == null) {
			String nombreImagen = upload.saveImages(file, servicio.getNombre());
			servicio.setImagen(nombreImagen);
		}
		servicioService.save(servicio);
		return "redirect:/servicios";
	}

	// metodo para llenar los imputs de la vista edit
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Servicio s = new Servicio();
		Optional<Servicio> os = servicioService.get(id);
		s = os.get();
		LOGGER.info("Busqueda de servicio por id {}", s);
		model.addAttribute("servicio", s);
		return "servicios/edit";
	}

	// metodo para actualizar los datos de un servicio
	@PostMapping("/update")
	public String update(Servicio servicio, @RequestParam("img") MultipartFile file) throws IOException {
		LOGGER.info("Este es el objeto del servicio a actualizar en la DB {}", servicio);
		Servicio s = new Servicio();
		s = servicioService.get(servicio.getId()).get();
		if (file.isEmpty()) {
			servicio.setImagen(s.getImagen());
		} else {
			if (!s.getImagen().equals("default.jpg")) {
				upload.deleteImage(s.getImagen());
			}
			String nombreImagen = upload.saveImages(file, s.getNombre());
			servicio.setImagen(nombreImagen);
		}
		servicio.setUsuario(s.getUsuario());
		servicioService.update(servicio);
		return "redirect:/servicios";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id) {
		Servicio s = new Servicio();
		s = servicioService.get(id).get();
		if (!s.getImagen().equals("default.jpg")) {
			upload.deleteImage(s.getImagen());
		}
		servicioService.delete(id);
		return "redirect:/servicios";
	}

}
