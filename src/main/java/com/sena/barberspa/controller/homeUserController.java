package com.sena.barberspa.controller;

import java.util.ArrayList;
import java.util.List;
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

import com.sena.barberspa.model.DetalleOrden;
import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Producto;
import com.sena.barberspa.service.IProductoService;

@Controller
@RequestMapping("/") // La raiz del proyecto
public class homeUserController {

	// Instancia de LOGGER para ver datos por consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(homeUserController.class);

	// Instancia de objeto - Servicio
	@Autowired
	private IProductoService productoService;

	// dos variables
	// lista de detallles de la orden para guardarlos en la database
	List<DetalleOrden> detalles = new ArrayList<DetalleOrden>();

	// objeto que almacena los datos de la orden
	Orden orden = new Orden();

	// Metodo que mapea la vista de usuario en la raiz del proyecto
	@GetMapping("")
	public String home(Model model) {
		return "usuario/home";
	}
	@GetMapping("/mantenimiento")
	public String mantenimiento(Model model) {
		return "usuario/mantenimiento";
	}
	
	
	@GetMapping("/productosVista")
	public String productosVista(Model model) {
		model.addAttribute("productos", productoService.findAll());
		return "usuario/productosVista";
	}
	@GetMapping("/serviciosVista")
	public String serviciosVista(Model model) {
		return "usuario/serviciosVista";
	}

	// Metodo que carga el producto de usuario con el id
	@GetMapping("productoHome/{id}")
	public String productoHome(@PathVariable Integer id, Model model) {
		LOGGER.info("ID producto enviado como parametro {}", id);
		// Variable de clase producto
		Producto p = new Producto();
		// objeto de tipo optional
		Optional<Producto> op = productoService.get(id);
		// pasar el producto
		p = op.get();
		// enviar a la vista con el model los detalles del producto
		model.addAttribute("producto", p);
		return "usuario/productoHome";
	}

	// metodo para enviardel boton de productohome al carrito
	@PostMapping("/cart")
	public String addCart(@RequestParam Integer id, @RequestParam Double cantidad, Model model) {
		DetalleOrden detaorden = new DetalleOrden();
		Producto p = new Producto();
		// variable que siempre que este en el metodo inicializa en cero despues de cada
		// compra
		double sumaTotal = 0;
		Optional<Producto> op = productoService.get(id);
		LOGGER.info("Producto añadido: {}", op.get());
		LOGGER.info("Cantidad añadida: {}", cantidad);
		p = op.get();
		detaorden.setCantidad(cantidad);
		detaorden.setPrecio(p.getPrecio());
		detaorden.setNombre(p.getNombreproducto());
		detaorden.setTotal(p.getPrecio() * cantidad);
		detaorden.setProducto(p);
		// validacion para evitar duplicados de productos
		Integer idProducto = p.getId();
		// funcion lamda stream y funcion anonima con predicado anyMatch
		boolean insertado = detalles.stream().anyMatch(prod -> prod.getProducto().getId() == idProducto);
		// si no es true añade el producto
		if (!insertado) {
			// detalles
			detalles.add(detaorden);
		}

		// suma de totales de la lsita que el usuario añada al carrito
		// funcion de java 8 lamda stream
		// funcion de java 8 anonima dt
		sumaTotal = detalles.stream().mapToDouble(dt -> dt.getTotal()).sum();
		// pasar variables a la vista
		orden.setTotal(sumaTotal);
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		return "usuario/carrito";
	}

	// METODO PARA QUITAR PRODUCTOS DEL CARRITO
	@GetMapping("/delete/cart/{id}")
	public String deleteProductoCart(@PathVariable Integer id, Model model) {
		// lista nueva de productos
		List<DetalleOrden> ordenesNuevas = new ArrayList<DetalleOrden>();
		// Quitar objeto de la lista de detalleOrden
		for (DetalleOrden detalleOrden : detalles) {
			if (detalleOrden.getProducto().getId() != id) {
				ordenesNuevas.add(detalleOrden);
			}
		}
		// poner la nueva lista con los productos restantes del carrito
		detalles = ordenesNuevas;
		// recalcular los productos del carrito
		double sumaTotal = 0;
		sumaTotal = detalles.stream().mapToDouble(dt -> dt.getTotal()).sum();
		// pasar variables a la vista
		orden.setTotal(sumaTotal);
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		return "usuario/carrito";

	}

	// metodo para redirigir al carrito sin productos
	public String getCart(Model model) {
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		return "usuario/carrito";
	}

}
