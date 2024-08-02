package com.springbootfproject.firstWebApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.springbootfproject.firstWebApp.todomodel.Todo;

import jakarta.transaction.Transactional;
@SessionAttributes("username")
public interface TodoRepository extends JpaRepository<Todo, Integer> {

	public List<Todo> findByUsername(String username);

	@Modifying
    @Transactional
    @Query("DELETE FROM Todo t WHERE t.username = :username")
    int deleteByUsername(@Param("username") String username);

}
