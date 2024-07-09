package com.springbootfproject.firstWebApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.springbootfproject.firstWebApp.todomodel.Todo;
@SessionAttributes("username")
public interface TodoRepository extends JpaRepository<Todo, Integer> {

	public List<Todo> findByUsername(String username);

//	@Query("SELECT t FROM todos.todo t WHERE (:time IS NULL OR t.time = :time) AND (:received IS NULL OR t.received = :received) AND (:month IS NULL OR MONTH(t.target_date) = :month)")
//	List<Todo> filterTodos(String time, Boolean received, Integer month);


}
