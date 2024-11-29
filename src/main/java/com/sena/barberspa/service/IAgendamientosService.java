package com.sena.barberspa.service;

import java.util.List;
import java.util.Optional;

import com.sena.barberspa.model.Agendamiento;

public interface IAgendamientosService {
	public Agendamiento save(Agendamiento agendamiento);

	public Optional<Agendamiento> get(Integer id);

	public void update(Agendamiento agendamiento);

	public void delete(Integer id);

	public List<Agendamiento> findAll();
}
