package com.sena.barberspa.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sena.barberspa.model.Producto;
import com.sena.barberspa.model.Servicio;
import com.sena.barberspa.model.Sucursal;
import com.sena.barberspa.service.IProductoService;
import com.sena.barberspa.service.IServiciosService;
import com.sena.barberspa.service.ISucursalesService;


@Controller
@RequestMapping("/administrador")
public class administradorController {
	
	@Autowired
	private IProductoService productoService;
	
	@Autowired
	private IServiciosService servicioService;
	
	@Autowired
	private ISucursalesService sucursalService;

	@GetMapping("")
	public String home(Model model) {
		List<Producto> productos = productoService.findAll();
		model.addAttribute("productos", productos);
		List<Servicio> servicios = servicioService.findAll();
		model.addAttribute("servicios", servicios);
		List<Sucursal> sucursales = sucursalService.findAll();
		model.addAttribute("sucursales", sucursales);
		return "administrador/home";

	}
	@GetMapping("/profile")
	public String profile(Model model) {
		return "administrador/profile";

	}

}
