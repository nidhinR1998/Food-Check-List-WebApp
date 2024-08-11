package com.springbootfproject.firstWebApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.springbootfproject.firstWebApp.todomodel.Advance;

import jakarta.transaction.Transactional;

@SessionAttributes("username")
@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long>{

	List<Advance> findByUsername(String username);

	 @Modifying
	    @Transactional
	    @Query("UPDATE Advance a SET a.amount = :newTotalAdvanceAmount WHERE a.username = :username")
	    void updateTotalAdvanceAmount(@Param("newTotalAdvanceAmount") int newTotalAdvanceAmount, @Param("username") String username);

}
