package com.sena.barberspa.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IUsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

	private final Logger LOGGER = LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IOrdenService ordenService;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@GetMapping("/registro")
	public String showRegistrationForm(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "usuario/registro";
	}

	@PostMapping("/save")
	public String registerUser(Usuario usuario, RedirectAttributes redirectAttributes) {
		try {
			usuario.setTipo("USER");
			usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
			usuarioService.save(usuario);
			redirectAttributes.addFlashAttribute("success", "Registro exitoso! Por favor inicie sesión.");
			return "redirect:/usuario/login";
		} catch (Exception e) {
			LOGGER.error("Error al registrar usuario: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Error al registrar. Intente nuevamente.");
			return "redirect:/usuario/registro";
		}
	}

	@GetMapping("/login")
	public String showLoginForm(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "usuario/login";
	}

	// cambiar post por get en spring security
	// metodo de autentificacion 1 con postmapping sin spring security en post
	// metodo con spring security en get
	@GetMapping("/acceder")
	public String acceder(Usuario usuario, HttpSession session) {
		LOGGER.info("Accesos: {}", usuario);
		// Optional<Usuario> userEmail = usuarioService.findByEmail(usuario.getEmail());
		Optional<Usuario> user = usuarioService
				.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()));
		LOGGER.info("Usuario db obtenido: {}", user.get());
		if (user.isPresent()) {
			session.setAttribute("idUsuario", user.get().getId());
			if (user.get().getTipo().equals("ADMIN")) {
				return "redirect:/administrador";
			} else {
				return "redirect:/";
			}
		} else {
			LOGGER.warn("Usuario no existe en DB");
		}
		return "redirect:/";
	}

	@GetMapping("/cerrar")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	@GetMapping("/compras")
	public String showUserOrders(HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		List<Orden> ordenes = ordenService.findByUsuario(usuario);
		model.addAttribute("ordenes", ordenes);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/compras";
	}

	@GetMapping("/compras/{id}")
	public String showOrderDetails(@PathVariable Integer id, HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Orden orden = ordenService.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada"));

		// Verificar que la orden pertenece al usuario
		if (!orden.getUsuario().getId().equals(Integer.parseInt(session.getAttribute("idUsuario").toString()))) {
			return "redirect:/usuario/compras";
		}

		model.addAttribute("detalles", orden.getDetalle());
		model.addAttribute("orden", orden);
		model.addAttribute("sesion", session.getAttribute("idUsuario"));

		return "usuario/detallecompra";
	}

	@GetMapping("/perfil")
	public String showProfile(HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		model.addAttribute("usuario", usuario);
		return "usuario/perfil";
	}

	@GetMapping("/editar")
	public String showEditForm(HttpSession session, Model model) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		model.addAttribute("usuario", usuario);
		return "usuario/editar";
	}

	@PostMapping("/actualizar")
	public String updateProfile(Usuario usuario, HttpSession session, RedirectAttributes redirectAttributes) {
		if (session.getAttribute("idUsuario") == null) {
			return "redirect:/usuario/login";
		}

		try {
			Usuario existingUser = usuarioService
					.findById(Integer.parseInt(session.getAttribute("idUsuario").toString()))
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			// Actualizar solo los campos permitidos
			existingUser.setNombre(usuario.getNombre());
			existingUser.setEmail(usuario.getEmail());
			existingUser.setDireccion(usuario.getDireccion());
			existingUser.setTelefono(usuario.getTelefono());

			if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
				existingUser.setPassword(passwordEncoder.encode(usuario.getPassword()));
			}

			usuarioService.save(existingUser);
			redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
			return "redirect:/usuario/perfil";

		} catch (Exception e) {
			LOGGER.error("Error al actualizar perfil: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil");
			return "redirect:/usuario/editar";
		}
	}
}