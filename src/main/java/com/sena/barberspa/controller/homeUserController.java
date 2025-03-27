package com.sena.barberspa.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.sena.barberspa.model.Agendamiento;
import com.sena.barberspa.model.DetalleOrden;
import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Producto;
import com.sena.barberspa.model.Servicio;
import com.sena.barberspa.model.Sucursal;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IAgendamientosService;
import com.sena.barberspa.service.IDetalleOrdenService;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IProductoService;
import com.sena.barberspa.service.IServiciosService;
import com.sena.barberspa.service.ISucursalesService;
import com.sena.barberspa.service.IUsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/") // La raiz del proyecto
public class homeUserController {

	// Instancia de LOGGER para ver datos por consola
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(homeUserController.class);

	// Instancia de objeto - Servicio
	@Autowired
	private IProductoService productoService;

	@Autowired
	private IServiciosService servicioService;

	@Autowired
	private ISucursalesService sucursalService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IOrdenService ordenService;

	@Autowired
	private IDetalleOrdenService detalleOrdenService;

	@Autowired
	private IAgendamientosService agendamientosService;

	// dos variables
	// lista de detallles de la orden para guardarlos en la database
	List<DetalleOrden> detalles = new ArrayList<DetalleOrden>();

	// objeto que almacena los datos de la orden
	Orden orden = new Orden();

	// Metodo que mapea la vista de usuario en la raiz del proyecto
	@GetMapping("")
	public String home(Model model, HttpSession session) {
		LOGGER.info("Sesion usuario: {}", session.getAttribute("idUsuario"));
		model.addAttribute("productos", productoService.findAll());

		model.addAttribute("servicios", servicioService.findAll());

		model.addAttribute("sucursales", sucursalService.findAll());

		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/home";
	}

	@GetMapping("/mantenimiento")
	public String mantenimiento(Model model, HttpSession session) {
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/mantenimiento";
	}

	@GetMapping("/productosVista")
	public String productosVista(Model model, HttpSession session) {
		model.addAttribute("productos", productoService.findAll());
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/productosVista";
	}

	@GetMapping("/serviciosVista")
	public String serviciosVista(Model model, HttpSession session) {
		model.addAttribute("servicios", servicioService.findAll());
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/serviciosVista";
	}

	// metodo de creacion de agendamientos
	@PostMapping("/save")
	public String save(@RequestParam("servicio") Integer idServicio, @RequestParam("sucursal") Integer idSucursal,
			@RequestParam String fechaHora, @RequestParam String mensaje, HttpSession session)
			throws IOException {
		Agendamiento a = new Agendamiento();
		Optional<Servicio> os = servicioService.get(idServicio);
		Optional<Sucursal> osu = sucursalService.get(idSucursal);
		// Convertir la fecha de String a LocalDateTime (Spring lo espera en formato ISO
		// 8601)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		LocalDateTime fechaHoraAgendamiento = LocalDateTime.parse(fechaHora, formatter);
		a.setFechaHora(fechaHoraAgendamiento); // Asegúrate de que tu entidad Agendamiento tenga este campo
		a.setMensaje(mensaje);
		a.setServicio(os.get());
		a.setSucursal(osu.get());
		a.setEstado("SOLICITADA");
		Usuario u = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString())).get();
		a.setUsuario(u);
		LOGGER.info("Este es el objeto del agendamiento a guardar en la DB {}", a);
		agendamientosService.save(a);

		return "redirect:/";
	}

	// Metodo que carga el producto de usuario con el id
	@GetMapping("productoHome/{id}")
	public String productoHome(@PathVariable Integer id, Model model, HttpSession session) {
		LOGGER.info("ID producto enviado como parametro {}", id);
		// Variable de clase producto
		Producto p = new Producto();
		// objeto de tipo optional
		Optional<Producto> op = productoService.get(id);
		// pasar el productoS
		p = op.get();
		// enviar a la vista con el model los detalles del producto
		model.addAttribute("producto", p);
		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/productoHome";
	}

	// Metodo que carga el servicio de usuario con el id
	@GetMapping("servicioHome/{id}")
	public String servicioHome(@PathVariable Integer id, Model model, HttpSession session) {
		LOGGER.info("ID servicio enviado como parametro {}", id);
		// Variable de clase servicio
		Servicio s = new Servicio();
		// objeto de tipo optional
		Optional<Servicio> sv = servicioService.get(id);
		// pasar el servicio
		s = sv.get();
		// enviar a la vista con el model los detalles del servicio
		model.addAttribute("servicio", s);
		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		model.addAttribute("servicios", servicioService.findAll());

		return "usuario/servicioHome";
	}

	// metodo para enviardel boton de productohome al carrito
	@PostMapping("/cart")
	public String addCart(@RequestParam Integer id, @RequestParam Double cantidad, Model model, HttpSession session) {
		DetalleOrden detaorden = new DetalleOrden();
		Producto p = new Producto();
		// variable que siempre que este en el metodo inicializa en cero despues de cada
		// compra
		double sumaTotal = 0;
		Optional<Producto> op = productoService.get(id);
		LOGGER.info("Producto añadido: {}", op.get());
		LOGGER.info("Cantidad añadida:", cantidad);
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
		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		Optional<Usuario> user = usuarioService
				.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()));
		if (!user.isPresent()) {
			return "/usuario/login";
		} 
		return "usuario/carrito";
	}

	// METODO PARA QUITAR PRODUCTOS DEL CARRITO
	@GetMapping("/delete/cart/{id}")
	public String deleteProductoCart(@PathVariable Integer id, Model model, HttpSession session) {
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
		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/carrito";

	}

	@GetMapping("/getCart")
	// metodo para redirigir al carrito sin productos
	public String getCart(Model model, HttpSession session) {
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/carrito";
	}

	// Este es el metodo para pasar a la vista del resumen de la orden
	@GetMapping("/order")
	public String order(Model model, HttpSession session) {
		Usuario u = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString())).get();
		model.addAttribute("cart", detalles);
		model.addAttribute("orden", orden);
		model.addAttribute("usuario", u);
		// variable de session
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		return "usuario/resumenorden";
	}

	@GetMapping("/saveOrder")
	public String saveOrder(HttpSession session) {
		// guardar orden
		Date fechacreacion = new Date();
		orden.setFechacreacion(fechacreacion);
		orden.setNumero(ordenService.generarNumeroOrden());
		// usuario que se referencia en esa compra previamente logeado
		Usuario u = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString())).get();
		orden.setUsuario(u);
		orden.setEstado("PROCESO");
		ordenService.save(orden);
		// guardar detalles de la orden
		for (DetalleOrden dt : detalles) {
			dt.setOrden(orden);
			detalleOrdenService.save(dt);
		}
		// limpiar valores que no se añadan a la orden recien guardada
		orden = new Orden();
		detalles.clear();

		return "redirect:/";
	}

	// metodo post para buscar productos en la vista del home de usuarios
	@PostMapping("/searchU")
	public String searchProducto(@RequestParam String nombreproducto, Model model) {
		LOGGER.info("nombre del producto: {}", nombreproducto);
		List<Producto> productos = productoService.findAll().stream()
				.filter(p -> p.getNombreproducto().toUpperCase().contains(nombreproducto.toUpperCase()))
				.collect(Collectors.toList());
		model.addAttribute("productos", productos);
		return "usuario/productosVista";
	}

}
