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
import com.sena.barberspa.model.Sucursal;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IServiciosService;
import com.sena.barberspa.service.ISucursalesService;
import com.sena.barberspa.service.IUsuarioService;
import com.sena.barberspa.service.UploadFileService;

@Controller
@RequestMapping("/sucursales")
public class SucursalController {

	// Instancia de LOGGER para ver datos en consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(SucursalController.class);

	@Autowired
	private ISucursalesService sucursalesService;

	// microsevicio imagenes

	@Autowired
	private UploadFileService upload;

	// metodo para redirigir a la vista show en el template de productos
	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("sucursales", sucursalesService.findAll());
		return "sucursales/show";
	}

	// metodo es el que redirige a la vista de creacion de productos
	@GetMapping("/create")
	public String create() {                                                                                                                                                                                                                                                                                                                                                         
		return "sucursales/create";
	}

	// metodo de creacion de productos
	@PostMapping("/save")
	public String save(Sucursal sucursal, @RequestParam("img") MultipartFile file) throws IOException {
		LOGGER.info("Este es el objeto de la sucursal a guardar en la DB {}", sucursal);
		
		// validacion imagen del producto

		if (sucursal.getId() == null) {
			String nombreImagen = upload.saveImages(file, sucursal.getNombre());
			sucursal.setImagen(nombreImagen);
		}
		sucursalesService.save(sucursal);
		return "redirect:/sucursales";
	}

	// metodo para llenar los imputs de la vista edit
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Sucursal s = new Sucursal();
		Optional<Sucursal> os = sucursalesService.get(id);
		s = os.get();
		LOGGER.info("Busqueda de sucursal por id {}", s);
		model.addAttribute("sucursal", s);
		return "sucursales/edit";
	}

	// metodo para actualizar los datos de un producto
	@PostMapping("/update")
	public String update(Sucursal sucursal, @RequestParam("img") MultipartFile file) throws IOException {
		LOGGER.info("Este es el objeto de la sucursal a actualizar en la DB {}", sucursal);
		Sucursal s = new Sucursal();
		s = sucursalesService.get(sucursal.getId()).get();
		if (file.isEmpty()) {
			sucursal.setImagen(s.getImagen());
		} else {
			if (!s.getImagen().equals("default.jpg")) {
				upload.deleteImage(s.getImagen());
			}
			String nombreImagen = upload.saveImages(file, s.getNombre());
			sucursal.setImagen(nombreImagen);
		}
		sucursalesService.update(sucursal);
		return "redirect:/sucursales";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id) {
		Sucursal s = new Sucursal();
		s = sucursalesService.get(id).get();
		if (!s.getImagen().equals("default.jpg")) {
			upload.deleteImage(s.getImagen());
		}
		sucursalesService.delete(id);
		return "redirect:/sucursales";
	}

}
