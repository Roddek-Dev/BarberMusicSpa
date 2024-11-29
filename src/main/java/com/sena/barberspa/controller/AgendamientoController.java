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

import com.sena.barberspa.model.Agendamiento;

import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IAgendamientosService;

@Controller
@RequestMapping("/agendamientos")
public class AgendamientoController {

	// Instancia de LOGGER para ver datos en consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(AgendamientoController.class);

	@Autowired
	private IAgendamientosService agendamientosService;

	// metodo para redirigir a la vista show en el template de productos
	@GetMapping("")
	public String show(Model model) {
		model.addAttribute("agendamientos", agendamientosService.findAll());
		return "agendamientos/show";
	}

	// metodo es el que redirige a la vista de creacion de productos
	@GetMapping("/create")
	public String create() {
		return "usuario";
	}

	// metodo de creacion de productos
	@PostMapping("/save")
	public String save(Agendamiento agendamiento) throws IOException {
		LOGGER.info("Este es el objeto del agendamiento a guardar en la DB {}", agendamiento);
		Usuario u = new Usuario(1, "", "", "", "", "", "", "");
		agendamiento.setUsuario(u);

		agendamientosService.save(agendamiento);
		return "redirect:/agendamientos";
	}

	// metodo para llenar los imputs de la vista edit
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model) {
		Agendamiento s = new Agendamiento();
		Optional<Agendamiento> os = agendamientosService.get(id);
		s = os.get();
		LOGGER.info("Busqueda de sucursal por id {}", s);
		model.addAttribute("agendamiento", s);
		return "agendamientos/edit";
	}

	// metodo para actualizar los datos de un producto
	@PostMapping("/update")
	public String update(Agendamiento agendamiento) throws IOException {
		LOGGER.info("Este es el objeto de la sucursal a actualizar en la DB {}", agendamiento);
		Agendamiento s = new Agendamiento();
		s = agendamientosService.get(agendamiento.getId()).get();
		agendamiento.setUsuario(s.getUsuario());
		agendamientosService.update(agendamiento);
		return "redirect:/agendamientos";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Integer id) {
		agendamientosService.delete(id);
		return "redirect:/agendamientos";
	}

}
