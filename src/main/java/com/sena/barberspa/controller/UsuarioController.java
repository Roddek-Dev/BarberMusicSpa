package com.sena.barberspa.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sena.barberspa.model.Orden;
import com.sena.barberspa.model.Usuario;
import com.sena.barberspa.service.IOrdenService;
import com.sena.barberspa.service.IUsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IOrdenService ordenService;

	// nuevo objeto encriptador
	BCryptPasswordEncoder passEncode = new BCryptPasswordEncoder();

	@GetMapping("/registro")
	public String createUser() {
		return "usuario/registro";
	}

	@PostMapping("/save")
	public String save(Usuario usuario, Model model) {
		LOGGER.info("Usuario a registrar: {}", usuario);
		usuario.setTipo("USER");
		// encriptador de contraseña
		usuario.setPassword(passEncode.encode(usuario.getPassword()));
		usuarioService.save(usuario);
		return "redirect:/";
	}

	@GetMapping("/login")
	public String login() {
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
	public String cerrarSession(HttpSession session) {
		session.removeAttribute("idUsuario");
		return "redirect:/";
	}

	// metodo para redirigir a la vista de compras de usuario
	@GetMapping("/compras")
	public String compras(HttpSession session, Model model) {
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idUsuario").toString())).get();
		List<Orden> ordenes = ordenService.findByUsuario(usuario);
		model.addAttribute("ordenes", ordenes);
		return "usuario/compras";
	}

	@GetMapping("/detalle/{id}")
	public String detalleCompra(HttpSession session, Model model, @PathVariable Integer id) {
		// sesion de usuario o id usuario
		model.addAttribute("sesion", session.getAttribute("idUsuario"));
		LOGGER.info("id de la orden: {}", id);
		Optional<Orden> orden = ordenService.findById(id);
		model.addAttribute("detalles", orden.get().getDetalle());
		return "usuario/detallecompra";
	}

	// Método para llenar los inputs en la vista de edición
	@GetMapping("/edit/{id}")
	public String editProfile(@PathVariable Integer id, Model model, HttpSession session) {
		Usuario usuarioActivo = (Usuario) session.getAttribute("usuario");

		if (usuarioActivo == null) {
			LOGGER.warn("Intento de edición sin sesión activa.");
			return "redirect:/login";
		}

		Optional<Usuario> op = usuarioService.get(id);
		if (op.isPresent()) {
			Usuario usuario = op.get();
			if (!usuarioActivo.getId().equals(id)) {
				LOGGER.warn("Intento de edición de otro usuario.");
				return "redirect:/perfil";
			}

			LOGGER.info("Edición de perfil para usuario con ID {}", id);
			model.addAttribute("usuario", usuario);
			return "usuarios/edit";
		} else {
			LOGGER.warn("Usuario con ID {} no encontrado", id);
			return "redirect:/usuarios";
		}
	}

	// Método para actualizar el perfil
	@PostMapping("/update")
	public String updateProfile(@ModelAttribute Usuario usuario, HttpSession session) {
		Usuario usuarioActivo = (Usuario) session.getAttribute("usuario");

		if (usuarioActivo == null || !usuarioActivo.getId().equals(usuario.getId())) {
			LOGGER.warn("Intento de actualización no autorizado.");
			return "redirect:/login";
		}

		usuarioService.save(usuario);
		session.setAttribute("usuario", usuario); // Actualizar la sesión con los nuevos datos
		LOGGER.info("Perfil actualizado para usuario con ID {}", usuario.getId());
		return "redirect:/perfil";
	}
}