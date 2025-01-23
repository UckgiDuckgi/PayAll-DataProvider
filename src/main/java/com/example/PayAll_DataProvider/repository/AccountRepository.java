package com.example.PayAll_DataProvider.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.PayAll_DataProvider.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	@Query("SELECT a FROM Account a WHERE a.user.id = :userId")
	List<Account> findByUserId(@Param("userId") Long userId);

	Optional<Account> findFirstByUserId(Long userId);
}
