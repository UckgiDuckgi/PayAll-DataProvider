package com.example.PayAll_DataProvider.entity;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_basic_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBasicInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20)
	private String accountNum;

	@Column(nullable = false)
	private Date baseDate;

	@Column(nullable = false, length = 3)
	private String currencyCode;

	@Column(nullable = false, precision = 18, scale = 3)
	private BigDecimal withholdingsAmt;

	@Column(precision = 18, scale = 3)
	private BigDecimal creditLoanAmt;

	@Column(precision = 18, scale = 3)
	private BigDecimal mortgageAmt;

	@Column(nullable = false, precision = 18, scale = 3)
	private BigDecimal availBalance;

	@OneToOne
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
}
