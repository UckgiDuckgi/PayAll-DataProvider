package com.example.PayAll_DataProvider.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PayAll_DataProvider.entity.AccountTransaction;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction,Long> {
	@Query("SELECT t FROM AccountTransaction t WHERE t.accountNum = :accountNum AND t.transDtime BETWEEN :fromDate AND :toDate")
	List<AccountTransaction> findTransactions(
		@Param("accountNum") String accountNum,
		@Param("fromDate") Timestamp fromDate,
		@Param("toDate") Timestamp toDate,
		@Param("limit") int limit,
		@Param("nextPage") String nextPage
	);
}
